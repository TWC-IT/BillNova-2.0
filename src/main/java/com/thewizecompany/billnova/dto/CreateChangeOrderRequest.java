package com.thewizecompany.billnova.dto;


import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateChangeOrderRequest {
    @NotNull(message = "Project id is required")
    private UUID projectId;
    private String description;
    @NotNull(message = "Project id is required")
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
