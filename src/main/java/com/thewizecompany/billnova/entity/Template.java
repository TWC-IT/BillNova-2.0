package com.thewizecompany.billnova.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(
        name = "templates",
        indexes = {
                @Index(
                        name = "idx_templates_is_deleted",
                        columnList = "is_deleted"
                ),
                @Index(
                        name = "idx_templates_name",
                        columnList = "name"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template extends BaseEntity {

    @Column(
            name = "name"
    )
    private String name;
    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;
    @Column(
            name = "is_default",
            nullable = false
    )
    @Builder.Default
    private boolean isDefault = false;
}
