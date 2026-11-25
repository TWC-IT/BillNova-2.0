package com.thewizecompany.billnova.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "payment_allocations"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAllocation extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal allocatedAmount;
}
