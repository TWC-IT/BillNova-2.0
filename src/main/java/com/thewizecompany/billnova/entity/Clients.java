package com.thewizecompany.billnova.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(
        name = "clients",
       indexes ={
                @Index(
                        name = "idx_clients_email",
                        columnList = "email"
                )
       },
       uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_clients_email",
                        columnNames = "email"
                )
       }

)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clients extends BaseEntity {
    @Column(
            name = "name",
            nullable = false
    )
    private String name;
    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;
    @Column(
            name = "billing_address",
            nullable = false
    )
    private String billingAddress;
}
