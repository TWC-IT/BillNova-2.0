package com.thewizecompany.billnova.repository;

import com.thewizecompany.billnova.entity.Clients;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Clients, UUID> {
    boolean existsByEmail(@Email(message = "Email is required") String Email);

    Optional<Clients> findByIdAndIsDeletedFalse(UUID clientId);

    Page<Clients> findAllByIsDeletedFalse(Pageable pageable);
}
