package com.thewizecompany.billnova.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {
    private final UUID id;
    private final String paymentCode;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final String metaData;
}
