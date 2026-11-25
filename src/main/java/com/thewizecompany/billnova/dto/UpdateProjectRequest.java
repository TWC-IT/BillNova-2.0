package com.thewizecompany.billnova.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class UpdateProjectRequest {
    private String name;
    private String location;
    private BigDecimal contractAmount;
    private UUID clientContactId;
}
