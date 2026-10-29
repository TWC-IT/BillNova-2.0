package com.thewizecompany.billnova.repository;

import com.thewizecompany.billnova.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndIsRevokedFalseAndIsDeletedFalse(String tokenHash);


    @Modifying(clearAutomatically = true)
    @Query("""
UPDATE RefreshToken r
SET r.isRevoked = TRUE
WHERE r.userId = :id
AND r.isRevoked = FALSE
""")
    void revokeAllForUser(UUID id);
}
