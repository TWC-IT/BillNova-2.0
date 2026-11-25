package com.thewizecompany.billnova.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.thewizecompany.billnova.entity.Invoice;
import com.thewizecompany.billnova.entity.PaymentAllocation;
import com.thewizecompany.billnova.entity.Template;
import com.thewizecompany.billnova.exception.BusinessException;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final TemplateService templateService;
    private final TemplateRepository templateRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final ClientContactRepository clientContactRepository;
    private final SpringTemplateEngine templateEngine;
    private final ChangeOrderRepository changeOrderRepository;

    private  static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(
            UUID invoiceId,
            UUID templateId) {

        Invoice invoice = invoiceRepository
                .findByIdAndIsDeletedFalse(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice", invoiceId.toString()
                        )
                );

        if (templateId == null){
            templateId = templateRepository.getDefaultTemplateId()
                    .orElseThrow(() -> new BusinessException("Provide template id or set a default template"));
        }

        UUID finalTemplateId = templateId;
        Template template = templateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: ", finalTemplateId.toString()));

        /*
         * Build data context.
         * All variables accessible in template as ${variable}
         */
        Context ctx = new Context();

        /*
         * Invoice fields.
         */
        ctx.setVariable("companyLogo", loadLogoAsBase64());
        ctx.setVariable("invoiceNumber",
                invoice.getCode());
        ctx.setVariable("invoiceDate",
                invoice.getInvoiceDate().format(DATE_FORMAT));
        ctx.setVariable("dueDate",
                invoice.getInvoiceDate() != null
                        ? invoice.getInvoiceDate().format(DATE_FORMAT)
                        : "N/A");
        ctx.setVariable("targetType", invoice.getInvoiceType().name());
        ctx.setVariable("status",
                invoice.getStatus().name());
        ctx.setVariable("subtotal",
                invoice.getAmount());

        ctx.setVariable("totalAmount",
                invoice.getAmount());
        ctx.setVariable("amountPaid",
                invoice.getAmountPaid());
        ctx.setVariable("outstandingAmount",
                invoice.getRemainingAmount());

        ctx.setVariable("paymentStatus",
                invoice.getRemainingAmount().compareTo(BigDecimal.ZERO)
                        == 0 ? "Full & Final" : "Partial");

        ctx.setVariable("discount", "0");
        ctx.setVariable("shipping", "0");

        /*
         * Client fields.
         */
        var client = clientRepository
                .findByIdAndIsDeletedFalse(invoice.getClientId())
                .orElse(null);

        ctx.setVariable("clientName",
                client != null ? client.getName() : "");


        assert client != null;
        ctx.setVariable("clientAddress",
                client.getBillingAddress());
        ctx.setVariable("clientEmail", client.getEmail());

        /*
         * Change Order Fields
         */
        var changeOrder = changeOrderRepository.findByIdAndIsDeletedFalse(
                invoice.getCorId()
        ).orElse(null);
        ctx.setVariable("changeOrderNumber",
                changeOrder != null ? changeOrder.getCode() : "");
        ctx.setVariable("changeOrderAmount",
                changeOrder!=null ? changeOrder.getAmount() : "");

        /*
         * Project fields.
         */
        var project = projectRepository
                .findByIdAndIsDeletedFalse(
                        invoice.getProjectId()
                )
                .orElse(null);
        ctx.setVariable("projectNumber",
                project != null ? project.getCode() : "");
        ctx.setVariable("projectName",
                project != null ? project.getName() : "");
        ctx.setVariable("contractAmount",
                project != null ? project.getContractAmount() : "");
        ctx.setVariable("remainingToInvoice", project != null ? project.getTotalAmount().subtract(project.getAmountReceived()) : "");

        //Previous Invoices List
        List<Invoice> previousInvoices = null;
        BigDecimal previousTotal = BigDecimal.ZERO;
        if(project != null){
            previousInvoices = invoiceRepository.findByProjectIdAndIsDeletedFalse(project.getId());
            previousInvoices.remove(invoice);
            previousInvoices = previousInvoices.stream()
                    .filter(i -> Objects.equals(i.getInvoiceType(), invoice.getInvoiceType())).toList();
            previousTotal = previousInvoices.stream()
                    .map(Invoice::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        ctx.setVariable("previousInvoices", previousInvoices != null ? previousInvoices : Collections.emptyList());
        ctx.setVariable("previousTotal", previousTotal);



        /*
         * Payments.
         */
        var allocations = invoice.getAllocations();

        var payments = allocations.stream().map(PaymentAllocation::getPayment).toList();
        ctx.setVariable("payments", payments);

        /*
         * Company info (WizeVision).
         */
        ctx.setVariable("companyName", "The Wize Company");
        ctx.setVariable("companyAddress",
                "House No-54, Lane No. 4, Indraprasth Colony, Dehradun, Uttarakhand - 248001, IN");
        ctx.setVariable("companyGst", "Your GST Number");
        ctx.setVariable("companyEmail",
                "info@thewizecompany.com");
        ctx.setVariable("companyPhone", "+91 XXXXXXXXXX");

        return renderToPdf(
                template.getContent(), ctx
        );
    }

    private String loadLogoAsBase64() {

        try {
            ClassPathResource resource =
                    new ClassPathResource("static/LOGO_01.png");

            if (!resource.exists()) {
                log.error("Company logo not found");
                return "";
            }

            byte[] imageBytes = resource.getInputStream().readAllBytes();

            return Base64.getEncoder()
                    .encodeToString(imageBytes);

        } catch (IOException e) {
            log.error("Failed to load company logo", e);
            return "";
        }
    }

    private byte[] renderToPdf(
            String templateContent,
            Context context) {

        try {
            /*
             * STEP 1: Render HTML using Thymeleaf.
             * Replaces ${variable} expressions with real values.
             */
            String renderedHtml = templateEngine
                    .process(templateContent, context);

            /*
             * STEP 2: Convert rendered HTML to PDF
             * using OpenHTMLToPDF.
             */
            try (ByteArrayOutputStream outputStream =
                         new ByteArrayOutputStream()) {

                PdfRendererBuilder builder =
                        new PdfRendererBuilder();

                builder.useFastMode();
                builder.withHtmlContent(
                        renderedHtml,
                        null   // base URI — null is fine for
                        // inline CSS templates
                );
                builder.toStream(outputStream);
                builder.run();

                return outputStream.toByteArray();
            }

        } catch (Exception e) {
            log.error(
                    "PDF generation failed: {}",
                    e.getMessage(), e
            );
            throw new BusinessException(
                    "Failed to generate PDF: " + e.getMessage()
            );
        }
    }
}
