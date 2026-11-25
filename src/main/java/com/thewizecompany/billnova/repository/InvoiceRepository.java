package com.thewizecompany.billnova.repository;

import com.thewizecompany.billnova.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByProjectIdAndIsDeletedFalse(UUID projectId);

    Optional<Invoice> findByIdAndIsDeletedFalse(UUID invoiceId);

    @Query(
            value = """
            SELECT * FROM invoices
            WHERE is_deleted = FALSE
            AND (CAST(:search AS VARCHAR) IS NULL
                 OR LOWER(code)
                    LIKE LOWER(CONCAT('%',:search,'%')))
            AND (CAST(:status AS VARCHAR) IS NULL
                 OR status = :status)
            AND (CAST(:clientId AS VARCHAR) IS NULL
                 OR client_id = CAST(:clientId AS UUID))
            AND (CAST(:projectId AS VARCHAR) IS NULL
                 OR project_id = CAST(:projectId AS UUID))
            ORDER BY invoice_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM invoices
            WHERE is_deleted = FALSE
            AND (CAST(:search AS VARCHAR) IS NULL
                 OR LOWER(code)
                    LIKE LOWER(CONCAT('%',:search,'%')))
            AND (CAST(:status AS VARCHAR) IS NULL
                 OR status = :status)
            AND (CAST(:clientId AS VARCHAR) IS NULL
                 OR client_id = CAST(:clientId AS UUID))
            AND (CAST(:projectId AS VARCHAR) IS NULL
                 OR project_id = CAST(:projectId AS UUID))
            """,
            nativeQuery = true
    )
    Page<Invoice> getAll(
            @Param("search") String search,
            @Param("status") String status,
            @Param("clientId") String clientId,
            @Param("projectId") String projectId,
            Pageable pageable
    );
}
