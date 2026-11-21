package com.thewizecompany.billnova.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateChangeOrderRequest {
    private String description;
    private BigDecimal amount;
}
