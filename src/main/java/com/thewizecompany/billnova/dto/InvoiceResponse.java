package com.thewizecompany.billnova.dto;

import com.thewizecompany.billnova.enums.InvoiceStatus;
import com.thewizecompany.billnova.enums.InvoiceType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class InvoiceResponse {

    private final String invoiceNumber;
    private final BigDecimal amount;
    private final InvoiceStatus status;
    private final UUID projectId;
    private final InvoiceType invoiceType;
    private final LocalDate invoiceDate;

}
