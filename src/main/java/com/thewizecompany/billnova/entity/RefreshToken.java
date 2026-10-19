package com.thewizecompany.billnova.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "auth_refresh_tokens",
        indexes = {
                @Index(
                        name = "idx_refresh_token_hash",
                        columnList = "token_hash",
                        unique = true
                ),
                @Index(
                        name = "idx_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_refresh_token_expiry",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity implements Persistable<UUID> {

    @Column(
            name = "user_id",
            nullable = false
    )
    private UUID userId;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "expiry",
            nullable = false
    )
    private Instant expiry;

    @Column(
            name = "is_revoked",
            nullable = false
    )
    @Builder.Default
    private Boolean isRevoked = false;

    @Override
    public boolean isNew() {return getUserId() == null;}

    public boolean isExpired() {return Instant.now().isAfter(expiry);}

    public boolean isValid(){return !isRevoked && !isExpired();}

    public void revoke() {this.isRevoked = true;}
}
