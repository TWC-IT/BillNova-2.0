package com.thewizecompany.billnova.entity;


import com.thewizecompany.billnova.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "projects",
        indexes = {
                @Index(
                        name = "idx_projects_client_id",
                        columnList = "client_id"
                ),
                @Index(
                        name = "idx_projects_client_contact_id",
                        columnList = "client_contact_id"
                ),
                @Index(
                        name = "idx_projects_code",
                        columnList = "code"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_projects_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    @Column(
            name = "code",
            nullable = false,
            unique = true
    )
    private String code;
    @Column(
            name = "project_name",
            nullable = false
    )
    private String name;
    @Column(
            name = "status",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.DRAFT;
    @Column(
            name = "location"
    )
    private String location;
    @Column(
            name = "contract_amount",
            nullable = false
    )
    @Builder.Default
    private BigDecimal contractAmount = BigDecimal.ZERO;
    @Column(
            name = "client_id",
            nullable = false
    )
    private UUID clientId;
    @Column(
            name = "client_contact_id",
            nullable = false
    )
    private UUID clientContactId;
    @Column(
            name = "invoiced_amount"
    )
    @Builder.Default
    private BigDecimal invoicedAmount = BigDecimal.ZERO;
    @Column(
            name = "total_amount"
    )
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
    @Column(
            name = "amount_received"
    )
    @Builder.Default
    private BigDecimal amountReceived = BigDecimal.ZERO;

}
