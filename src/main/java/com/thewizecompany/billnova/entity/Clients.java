package com.thewizecompany.billnova.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Table(
        name = "clients"
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clients extends BaseEntity {

    @Column(name = "name" , nullable = false)
    private String name;

    @Column(name = "billing_address", nullable = false)
    private String billingAddress;

    @Column(name ="email", nullable = false , unique = true)
    private String email;
}


