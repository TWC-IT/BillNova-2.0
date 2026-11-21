package com.thewizecompany.billnova.dto;

import com.thewizecompany.billnova.enums.ChangeOrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class ChangeOrderResponse {
    private UUID id;
    private String code;
    private ChangeOrderStatus status;
    private String description;
    private BigDecimal amount;
    private BigDecimal invoicedAmount;
    private BigDecimal receivedAmount;
}
