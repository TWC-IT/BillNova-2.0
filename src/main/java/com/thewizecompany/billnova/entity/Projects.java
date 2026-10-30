package com.thewizecompany.billnova.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(
        name = "projects"
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projects extends BaseEntity{

    @Column(name = "name")
    private String name;

    @Column(name = "project_number")
    private String projectNumber;

    @Column(name = "contract_amount")
    private String contractAmount;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Clients client;
}
