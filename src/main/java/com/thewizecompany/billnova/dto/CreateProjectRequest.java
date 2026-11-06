package com.thewizecompany.billnova.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter@Setter
public class CreateProjectRequest {
    @NotBlank(message = "Project name required")
    private String name;
    private String location;
    @NotNull(message = "Project Contract Amount is required")
    private BigDecimal contractAmount;
    @NotNull(message = "Client Id is required")
    private UUID clientId;
    @NotNull(message = "Client Contact Id is required")
    private UUID clientContactId;

}
