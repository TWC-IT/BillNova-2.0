package com.thewizecompany.billnova.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(
        name = "client_contacts",
        indexes ={
                @Index(
                        name = "idx_client_contacts_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_client_contacts_client_id",
                        columnList = "client_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_client_contacts_email",
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
public class ClientContact extends BaseEntity{

    @Column(
            name = "name",
            nullable =  false
    )
    private String name;

    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;

    @Column(
            name = "client_id",
            nullable = false
    )
    private UUID clientId;
}
