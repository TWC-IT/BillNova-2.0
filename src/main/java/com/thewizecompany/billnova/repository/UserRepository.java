package com.thewizecompany.billnova.repository;

import com.thewizecompany.billnova.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByUsernameAndIsDeletedFalseAndIsActiveTrue(String username);

//    @Modifying(clearAutomatically = true)
//    @Query("""
//            UPDATE Users u
//            SET u.lastLoginAt = CURRENT_TIMESTAMP,
//                u.failedLoginAttempts = 0,
//                u.accountLockedUntil = NULL
//            WHERE u.id = :id
//        """)
//    void updateLoginSuccess(
//            @Param("id") UUID id
//    );
}
