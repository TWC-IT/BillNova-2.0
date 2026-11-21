package com.thewizecompany.billnova.repository;


import com.thewizecompany.billnova.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByIdAndIsDeletedFalse(UUID id);

    List<Template> findAllByIsDeletedFalse();

    boolean existsByNameAndIsDeletedFalseAndTemplateNameIgnoreCase(String templateName);


    @Modifying(clearAutomatically = true)
    @Query(
            """
UPDATE templates t
SET t.isDefault = FALSE
WHERE t.isDefault = TRUE
AND t.isDeleted = FALSE
"""
    )
    void demoteCurrentDefault();

}
