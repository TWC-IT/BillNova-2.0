package com.thewizecompany.billnova.dto;

import com.thewizecompany.billnova.enums.InvoiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateInvoiceRequest {
    @NotNull(message = "project id is required")
    private UUID projectId;

    private UUID changeOrderId;

    @DecimalMin("0.01")
    private BigDecimal amount;
    @DecimalMin("0.01")
    private BigDecimal percentage;

    @NotNull(message = "Invoice Date is required")
    private LocalDate invoiceDate;

    @NotNull(message = "Invoice type is required")
    private InvoiceType invoiceType;

    private boolean isDraft = false;
}
