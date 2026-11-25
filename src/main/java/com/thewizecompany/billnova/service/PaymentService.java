package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.dto.PaymentRequest;
import com.thewizecompany.billnova.dto.PaymentResponse;
import com.thewizecompany.billnova.entity.Invoice;
import com.thewizecompany.billnova.entity.Payment;
import com.thewizecompany.billnova.entity.PaymentAllocation;
import com.thewizecompany.billnova.enums.InvoiceStatus;
import com.thewizecompany.billnova.enums.SequenceType;
import com.thewizecompany.billnova.exception.BusinessException;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.InvoiceRepository;
import com.thewizecompany.billnova.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SequenceService sequenceService;

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request){
        if (request.getAmount().equals(BigDecimal.ZERO)){
            throw new BusinessException("Payment amount must be greater than zero.");
        }
        Payment payment = Payment.builder()
                .code(generateCode())
                .paymentDate(request.getPaymentDate())
                .amount(request.getAmount())
                .metaData(request.getMetaData().isBlank() ? null : request.getMetaData().toLowerCase(Locale.ROOT).trim())
                .build();

        BigDecimal totalAllocated = BigDecimal.ZERO;

        for (PaymentRequest.Allocations allocation : request.getAllocations()){
            Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(allocation.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: ", allocation.getInvoiceId().toString()));

            if (invoice.getStatus().equals(InvoiceStatus.DRAFT)){
                throw new IllegalArgumentException("Invoice not issued");
            }

            if (allocation.getAllocatedAmount().compareTo(invoice.getRemainingAmount()) > 0){
                throw new BusinessException("Allocation exceeds remaining balance");
            }

            PaymentAllocation paymentAllocation = PaymentAllocation.builder()
                    .allocatedAmount(allocation.getAllocatedAmount())
                    .invoice(invoice)
                    .payment(payment)
                    .build();
            payment.addAllocation(paymentAllocation);
            invoice.getAllocations().add(paymentAllocation);
            invoice.updateStatus(paymentAllocation.getAllocatedAmount());

            totalAllocated = totalAllocated.add(allocation.getAllocatedAmount());
        }

        if (totalAllocated.compareTo(payment.getAmount()) > 0){
            throw new IllegalArgumentException("Total Allocated amount exceeds payment");
        }

        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentCode(payment.getCode())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .metaData(payment.getMetaData())
                .build();
    }

    @Transactional
    public void deletePayment(UUID paymentId){
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: ", paymentId.toString()));

        for (PaymentAllocation allocation : payment.getAllocations()){
            Invoice invoice = allocation.getInvoice();
            invoice.reversePayment(allocation.getAllocatedAmount());
            invoice.getAllocations().remove(allocation);
        }
        payment.markDeleted();
        paymentRepository.save(payment);
    }

    private String generateCode(){
        int year = Year.now().getValue();
        Integer sequence = sequenceService.nextSequence(
                SequenceType.PAYMENT, year
        );
        return String.format("PAY-%d-%03d", year, sequence);
    }
}
