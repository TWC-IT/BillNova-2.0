package com.thewizecompany.billnova.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateChangeOrderRequest {
    private UUID projectId;
    private String description;
    private BigDecimal amount;
}
