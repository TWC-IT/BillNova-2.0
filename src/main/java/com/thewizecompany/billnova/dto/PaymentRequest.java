package com.thewizecompany.billnova.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PaymentRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String metaData;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
    @Valid
    @NotEmpty(message = "At least one allocation is required")
    private List<Allocations> allocations;

    @Getter
    @Setter
    public static class Allocations{
        @NotNull(message = "Invoice Id is required")
        private UUID invoiceId;

        @NotNull(message = "Allocated amount is required")
        @DecimalMin(value = "0.01", message = "Allocated amount must be greater than zero")
        private BigDecimal allocatedAmount;
    }
}
