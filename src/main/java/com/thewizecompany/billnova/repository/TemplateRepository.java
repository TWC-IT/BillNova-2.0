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

    boolean existsByIsDeletedFalseAndNameIgnoreCase(String name);

    @Query("""
        SELECT t.id FROM Template t WHERE t.isDefault = TRUE AND t.isDeleted = FALSE
        """)
    Optional<UUID> getDefaultTemplateId();

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Template t
        SET t.isDefault = FALSE
        WHERE t.isDefault = TRUE
        AND t.isDeleted = FALSE
        """)
    void demoteCurrentDefault();

}
