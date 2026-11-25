package com.thewizecompany.billnova.entity;


import com.thewizecompany.billnova.enums.BillingStatus;
import com.thewizecompany.billnova.enums.InvoiceStatus;
import com.thewizecompany.billnova.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
            name = "cor_id"
    )
    private  UUID corId;
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
            name = "amount_paid",
            nullable = false
    )
    @Builder.Default
    private  BigDecimal amountPaid = BigDecimal.ZERO;
    @Column(
            name = "invoice_date",
            nullable = false
    )
    @Builder.Default
    private LocalDate invoiceDate = LocalDate.now();
    @Column(
            name = "billing_status",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    private BillingStatus billingStatus;
    @Column(
            name = "invoice_type",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    private InvoiceType invoiceType;
    @Column(
            name = "status",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAllocation> allocations = new ArrayList<>();

    public BigDecimal getRemainingAmount(){
        return amount.subtract(amountPaid);
    }

    public void updateStatus(BigDecimal amount){
        this.amountPaid = this.amountPaid.add(amount);

        if(this.amountPaid.compareTo(this.amount) >= 0){
            this.setStatus(InvoiceStatus.PAID);
        }else if (this.amountPaid.compareTo(BigDecimal.ZERO) > 0){
            this.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }else {
            this.setStatus(InvoiceStatus.ISSUED);
        }
    }

    public void reversePayment(BigDecimal amount) {
        this.amountPaid = this.amountPaid.subtract(amount);

        if (this.amountPaid.compareTo(BigDecimal.ZERO) < 0) {
            this.amountPaid = BigDecimal.ZERO;
        }

        if (this.amountPaid.compareTo(this.amount) >= 0) {
            this.status = InvoiceStatus.PAID;
        } else if (this.amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        } else {
            this.status = InvoiceStatus.ISSUED;
        }
    }
}
