package com.thewizecompany.billnova.entity;


import com.thewizecompany.billnova.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Table(
        name = "invoices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_indexes_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_invoices_code",
                        columnList = "code"
                ),
                @Index(
                        name = "idx_invoices_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_invoices_client_id",
                        columnList = "client_id"
                ),
                @Index(
                        name = "idx_invoices_project_id",
                        columnList = "project_id"
                )
        }
)
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invoice extends BaseEntity {

    @Column(
            name = "client_id",
            nullable = false
    )
    private UUID clientId;
    @Column(
            name = "project_id",
            nullable = false
    )
    private UUID projectId;
    @Column(
            name = "code",
            nullable = false,
            unique = true
    )
    private String code;
    @Column(
            name = "amount",
            nullable = false
    )
    private BigDecimal amount;
    @Column(
            name = "status",
            nullable = false
    )
    private InvoiceStatus status;
}
