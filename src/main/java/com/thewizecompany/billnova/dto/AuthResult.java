package com.thewizecompany.billnova.dto;


import com.thewizecompany.billnova.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthResult {
    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpiryMs;
    private final UUID userId;
    private final String fullName;
    private final String username;
    private final Role role;
}
