package com.thewizecompany.billnova.repository;

import com.thewizecompany.billnova.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdAndIsDeletedFalse(UUID paymentId);

}
