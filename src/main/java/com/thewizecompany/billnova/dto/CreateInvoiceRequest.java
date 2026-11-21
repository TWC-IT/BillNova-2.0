package com.thewizecompany.billnova.dto;

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
    @NotNull(message = "client id is required")
    private UUID clientId;
    @NotNull(message = "project id is required")
    private UUID projectId;

    @DecimalMin("0.01")
    private BigDecimal amount;
    @DecimalMin("0.01")
    private BigDecimal percentage;

    @NotNull(message = "Invoice Date is required")
    private LocalDate invoiceDate;

    private boolean isDraft = false;
}
