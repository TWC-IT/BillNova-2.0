package com.thewizecompany.billnova.dto;


import com.thewizecompany.billnova.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProjectResponse {
    private String code;
    private String name;
    private String location;
    private ProjectStatus status;
    private BigDecimal contractAmount;
    private BigDecimal invoicedAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountReceived;
    private String client;
    private String clientContact;
    private String clientContactEmail;
}
