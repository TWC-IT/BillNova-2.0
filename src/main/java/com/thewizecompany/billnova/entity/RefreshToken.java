package com.thewizecompany.billnova.entity;


import jakarta.persistence.*;
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

    @Column(
            name = "ip_address",
            length = 45
    )
    private String ipAddress;

    @Column(
            name = "device_info",
            length = 500
    )
    private String deviceInfo;

    @Override
    public boolean isNew() {return getId() == null;}

    public boolean isExpired() {return Instant.now().isAfter(expiry);}

    public boolean isValid(){return !isRevoked && !isExpired();}

    public void revoke() {this.isRevoked = true;}
}
