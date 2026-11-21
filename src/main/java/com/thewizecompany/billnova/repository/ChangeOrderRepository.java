package com.thewizecompany.billnova.repository;

import com.thewizecompany.billnova.entity.ChangeOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface ChangeOrderRepository extends JpaRepository<ChangeOrder, UUID> {

    Optional<ChangeOrder> findByIdAndIsDeletedFalse(UUID Id);

    Page<ChangeOrder> findAllByProjectIdAndIsDeletedFalse(UUID projectId, Pageable pageable);
}
