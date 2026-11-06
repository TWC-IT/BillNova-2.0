package com.thewizecompany.billnova.entity;


import com.thewizecompany.billnova.enums.ChangeOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "change_orders",
        indexes = {
                @Index(
                        name = "idx_change_orders_project_id",
                        columnList = "project_id"
                ),
                @Index(
                        name = "idx_change_orders_code",
                        columnList = "code"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_change_orders_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeOrder extends BaseEntity {
    @Column(
            name = "code",
            nullable = false,
            unique = true
    )
    private String code;
    @Column(
            name = "project_id",
            nullable = false
    )
    private UUID projectId;
    @Column(
            name = "amount",
            nullable = false
    )
    private BigDecimal amount;
    @Column(
            name = "invoiced_amount",
            nullable = false
    )
    private BigDecimal invoicedAmount;
    @Column(
            name = "received_amount",
            nullable = false
    )
    private BigDecimal receivedAmount;
    @Column(
            name = "status",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ChangeOrderStatus status = ChangeOrderStatus.DRAFT;
    @Column(
            name = "meta_data"
    )
    private String metaData;
}
