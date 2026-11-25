package com.thewizecompany.billnova.dto;

import com.thewizecompany.billnova.enums.InvoiceStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateInvoiceRequest {
    @DecimalMin("0.01")
    private BigDecimal amount;
    @DecimalMin("0.01")
    private BigDecimal percentage;
    private LocalDate invoiceDate;
    private InvoiceStatus status;
}
