package com.thewizecompany.billnova.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;

@Table(
        name = "client_contact"
)
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientsContact extends BaseEntity{

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private Clients client;
}
