package com.thewizecompany.billnova.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payments_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_payments_code",
                        columnList = "code"
                )
        }
)
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {
    @Column(
            name = "code",
            nullable = false,
            unique = true
    )
    private String code;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "unallocated_amount",
            precision = 19,
            scale = 2
    )
    private BigDecimal unallocatedAmount;

    @Column(
            name = "date",
            nullable = false
    )
    private LocalDate paymentDate;

    private String metaData;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAllocation> allocations = new ArrayList<>();

    public void addAllocation(PaymentAllocation allocation) {
        allocations.add(allocation);
        allocation.setPayment(this);
    }
}
