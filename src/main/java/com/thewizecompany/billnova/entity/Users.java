package com.thewizecompany.billnova.entity;

import com.thewizecompany.billnova.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Table(
        name = "users",
        indexes = {
                @Index(
                        name = "idx_username",
                        columnList = "username",
                        unique = true
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_username",
                        columnNames = "username"
                )
        }
)
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users extends BaseEntity{

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password_hash",
    nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",
    nullable = false)
    private Role role;

    @Column(
            name = "is_active",
            nullable = false
    )
    @Builder.Default
    private boolean active = true;

    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;

    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public boolean isAccountLocked(){
        if(accountLockedUntil == null){
            return false;
        }
        return Instant.now().isBefore(accountLockedUntil);
    }

    public void recordFailedLogin(){
        this.failedLoginAttempts++;
        if(this.failedLoginAttempts >= 5){
            this.accountLockedUntil = Instant.now().plusSeconds(1800);
        }
    }

    public void recordSuccessfulLogin(String ipAddress){
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ipAddress;
    }
}
