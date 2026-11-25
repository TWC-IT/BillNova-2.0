package com.thewizecompany.billnova.service;

import com.thewizecompany.billnova.dto.CreateInvoiceRequest;
import com.thewizecompany.billnova.dto.InvoiceResponse;
import com.thewizecompany.billnova.dto.UpdateInvoiceRequest;
import com.thewizecompany.billnova.entity.ChangeOrder;
import com.thewizecompany.billnova.entity.Clients;
import com.thewizecompany.billnova.entity.Invoice;
import com.thewizecompany.billnova.entity.Project;
import com.thewizecompany.billnova.enums.BillingStatus;
import com.thewizecompany.billnova.enums.InvoiceStatus;
import com.thewizecompany.billnova.enums.InvoiceType;
import com.thewizecompany.billnova.enums.SequenceType;
import com.thewizecompany.billnova.exception.BusinessException;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.ChangeOrderRepository;
import com.thewizecompany.billnova.repository.ClientRepository;
import com.thewizecompany.billnova.repository.InvoiceRepository;
import com.thewizecompany.billnova.repository.ProjectRepository;
import com.thewizecompany.billnova.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final SequenceService sequenceService;
    private final ChangeOrderRepository changeOrderRepository;

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request){
        Project project = projectRepository.findByIdAndIsDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: ", request.getProjectId().toString()));

        ChangeOrder cor = null;

        if(request.getInvoiceType().equals(InvoiceType.CHANGE_ORDER)){
            if(request.getChangeOrderId() == null){
                throw new BusinessException(
                        "Change order Id required for invoice type change order"
                );
            }
            cor = changeOrderRepository.findByIdAndIsDeletedFalse(request.getChangeOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Change order not found with id: ", request.getChangeOrderId().toString()));

            if (!request.getProjectId().equals(cor.getProjectId())){
                throw new BusinessException("Change order does not belong to the same project");
            }
        }
        // Extracting target amount
        BigDecimal targetAmount = extractTargetAmount(project, cor, request.getInvoiceType());
        BigDecimal finalAmount;
        if(request.getPercentage() != null && request.getPercentage().compareTo(BigDecimal.ZERO) > 0){
            finalAmount = targetAmount.multiply(request.getPercentage()).divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP);
        }else if( request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0){
            finalAmount = request.getAmount();
        }else{
            throw new BusinessException("Must provide valid amount or percentage for invoicing");
        }

        BigDecimal alreadyInvoiced = getAlreadyInvoiced(project, request.getInvoiceType(), cor);

        BigDecimal newTotalInvoiced = alreadyInvoiced.add(finalAmount);

        if(newTotalInvoiced.compareTo(targetAmount) > 0){
            BigDecimal remaining = targetAmount.subtract(alreadyInvoiced);
            throw new BusinessException(
                    "Overbilling, invoice amount cannot exceed Project/Change Order amount"
            );
        }

        BillingStatus billingStatus = newTotalInvoiced.compareTo(targetAmount) == 0 ?
                BillingStatus.FULL_AND_FINAL : BillingStatus.PARTIAL;

        Invoice invoice = Invoice.builder()
                .projectId(project.getId())
                .code(generateCode())
                .clientId(project.getClientId())
                .invoiceType(request.getInvoiceType())
                .invoiceDate(request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDate.now())
                .corId(cor != null ? cor.getId() : null)
                .amount(finalAmount)
                .billingStatus(billingStatus)
                .status(request.isDraft() ? InvoiceStatus.DRAFT : InvoiceStatus.ISSUED)
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        project.setInvoicedAmount(newTotalInvoiced);
        projectRepository.save(project);

        return InvoiceResponse.builder()
                .invoiceDate(saved.getInvoiceDate())
                .invoiceNumber(saved.getCode())
                .amount(saved.getAmount())
                .invoiceType(saved.getInvoiceType())
                .status(saved.getStatus())
                .projectId(saved.getProjectId())
                .build();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID invoiceId){
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: ", invoiceId.toString()));

        return InvoiceResponse.builder()
                .invoiceDate(invoice.getInvoiceDate())
                .invoiceNumber(invoice.getCode())
                .amount(invoice.getAmount())
                .invoiceType(invoice.getInvoiceType())
                .status(invoice.getStatus())
                .projectId(invoice.getProjectId())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAll(
            String search, String status, UUID clientId, UUID projectId, Pageable pageable
    ){
        Page<Invoice> page = invoiceRepository.getAll(search, status, clientId != null ? clientId.toString() : null,
                projectId != null ? projectId.toString() : null, pageable);

        return PageResponse.from(
                page.map(invoice -> InvoiceResponse.builder()
                        .invoiceDate(invoice.getInvoiceDate())
                        .invoiceNumber(invoice.getCode())
                        .amount(invoice.getAmount())
                        .invoiceType(invoice.getInvoiceType())
                        .status(invoice.getStatus())
                        .projectId(invoice.getProjectId())
                        .build())
        );
    }

    private BigDecimal getAlreadyInvoiced(Project project, InvoiceType invoiceType, ChangeOrder cor){
        return invoiceRepository.findByProjectIdAndIsDeletedFalse(project.getId())
                .stream().filter(i -> i.getInvoiceType().equals(invoiceType))
                .filter(i -> {
                    if(invoiceType.equals(InvoiceType.CHANGE_ORDER)){
                        return cor.getId() != null && cor.getId().equals(i.getCorId());
                    }
                    return true;
                })
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public InvoiceResponse update(UUID invoiceId, UpdateInvoiceRequest request){
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: ", invoiceId.toString()));

        if (invoice.getStatus().equals(InvoiceStatus.PAID) || invoice.getStatus().equals(InvoiceStatus.PARTIALLY_PAID)) {
            throw new BusinessException("Invoice have payments, cannot update");
        }

        if(invoice.getStatus().equals(InvoiceStatus.LOST)){
            throw new IllegalArgumentException("Cannot update Lost invoices");
        }
        BigDecimal newSubTotal = null;
        if(request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0){
            newSubTotal = request.getAmount();
        } else if(request.getPercentage() != null && request.getPercentage().compareTo(BigDecimal.ZERO) > 0){
            BigDecimal targetAmount = resolveTargetAmount(invoice);
            newSubTotal = targetAmount.multiply(request.getPercentage()).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
        }

        if (newSubTotal != null){
            BigDecimal otherInvoicesTotal = invoiceRepository.findByProjectIdAndIsDeletedFalse(
                    invoice.getProjectId()
            ).stream().filter(i -> !i.getId().equals(invoiceId)
            && i.getInvoiceType() == invoice.getInvoiceType()
            && isSameTarget(i, invoice))
                    .map(Invoice::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal targetAmount = resolveTargetAmount(invoice);
            BigDecimal newTotal = otherInvoicesTotal.add(newSubTotal);

            if (newTotal.compareTo(targetAmount) > 0){
                throw new BusinessException("Amount exceeding the remaining invoiceable amount.");
            }

            invoice.setAmount(newSubTotal);

            BillingStatus newBillingStatus = newTotal.compareTo(targetAmount) == 0 ? BillingStatus.FULL_AND_FINAL : BillingStatus.PARTIAL;
            invoice.setBillingStatus(newBillingStatus);
            invoice.setStatus(InvoiceStatus.DRAFT);
        }

        invoiceRepository.save(invoice);
        log.info("Invoice {} updated and saved as Draft", invoice.getCode());

        return InvoiceResponse.builder()
                .invoiceDate(invoice.getInvoiceDate())
                .invoiceNumber(invoice.getCode())
                .amount(invoice.getAmount())
                .invoiceType(invoice.getInvoiceType())
                .status(invoice.getStatus())
                .projectId(invoice.getProjectId())
                .build();

    }

    @Transactional
    public void delete(UUID invoiceId){
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with Id: ", invoiceId.toString()));

        if (invoice.getStatus().equals(InvoiceStatus.PAID)){
            throw new BusinessException("Invoice already paid cannot delete.");
        }
        if (invoice.getStatus().equals(InvoiceStatus.PARTIALLY_PAID)){
            throw new BusinessException("Invoice has payments, try marking as lost.");
        }

        invoice.markDeleted();
        invoiceRepository.save(invoice);
    }

    @Transactional
    public InvoiceResponse markLost(UUID invoiceId){
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: ", invoiceId.toString()));

        if (invoice.getStatus().equals(InvoiceStatus.PAID)){
            throw new BusinessException("Invoice already paid.");
        }
        if(invoice.getStatus().equals(InvoiceStatus.LOST)){
            throw new BusinessException("Invoice already lost");
        }
        invoice.setStatus(InvoiceStatus.LOST);
        invoiceRepository.save(invoice);
        log.info("Invoice {} marked lost", invoice.getCode());
        return InvoiceResponse.builder()
                .invoiceDate(invoice.getInvoiceDate())
                .invoiceNumber(invoice.getCode())
                .amount(invoice.getAmount())
                .invoiceType(invoice.getInvoiceType())
                .status(invoice.getStatus())
                .projectId(invoice.getProjectId())
                .build();
    }


    private boolean isSameTarget(Invoice a, Invoice b) {
        if (a.getInvoiceType() == InvoiceType.BASE_CONTRACT) {
            return true;
        }
        /*
         * CHANGE_ORDER: must be the same change order UUID.
         */
        if (a.getCorId() == null
                || b.getCorId() == null) {
            return false;
        }
        return a.getCorId()
                .equals(b.getCorId());
    }

    private BigDecimal resolveTargetAmount(Invoice invoice){
        if (invoice.getInvoiceType().equals(InvoiceType.BASE_CONTRACT)){
            return projectRepository.findByIdAndIsDeletedFalse(
                    invoice.getProjectId()
            ).map(Project::getContractAmount)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project not found by id: ", invoice.getProjectId().toString()
                    ));
        }

        return changeOrderRepository.findByIdAndIsDeletedFalse(
                invoice.getCorId()
        ).map(ChangeOrder::getAmount)
                .orElseThrow(() -> new ResourceNotFoundException("Change order not found with id: ", invoice.getCorId().toString()));
    }

    private BigDecimal extractTargetAmount(Project project, ChangeOrder cor, InvoiceType invoiceType){
        if (invoiceType.equals(InvoiceType.BASE_CONTRACT)){
            if(project.getContractAmount() == null || project.getContractAmount().compareTo(BigDecimal.ZERO) <= 0){
                throw new BusinessException("Project has no contract amount set");
            }
            return project.getContractAmount();
        }
        if(cor == null || cor.getAmount() == null || cor.getAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("Change order has no contract amount set");
        }

        return  cor.getAmount();
    }
    private String generateCode(){
        int year = Year.now().getValue();
        Integer sequence = sequenceService.nextSequence(
                SequenceType.INVOICE, year
        );
        return String.format("INV-%d-%03d", year, sequence);
    }
}
