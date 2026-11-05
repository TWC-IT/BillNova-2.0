package com.thewizecompany.billnova.repository;


import com.thewizecompany.billnova.entity.ClientContact;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientContactRepository extends JpaRepository<ClientContact, UUID> {

    boolean existsByEmail(@Email @NotBlank(message = "Email is required") String email);

    Optional<ClientContact> findByIdAndIsDeletedFalse(UUID uuid);

    Page<ClientContact> findByIsDeletedFalseAndClientId(UUID clientId, Pageable pageable);
}
