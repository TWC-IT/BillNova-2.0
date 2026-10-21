package com.thewizecompany.billnova.controller;


import com.thewizecompany.billnova.config.AppProperties;
import com.thewizecompany.billnova.dto.AuthResponse;
import com.thewizecompany.billnova.dto.AuthResult;
import com.thewizecompany.billnova.dto.LoginRequest;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppProperties appProperties;

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResult result = authService.login(request, extractIpAddress(httpRequest), extractDeviceInfo(httpRequest));

        httpResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                buildRefreshCookie(result.getRefreshToken()).toString()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        toAuthResponse(result),
                        "Login Successful"
                )
        );
    }

    //Helper Methods..........................................................

    private String extractIpAddress(HttpServletRequest httpRequest) {
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }

    private String extractDeviceInfo(HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        return userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent;
    }

    private boolean isProductionEnvironment(){
        String profile = System.getProperty("spring.profiles.active", "dev");
        return profile.contains("prod");
    }

    //Mapper Helper Function ........................................................
    private AuthResponse toAuthResponse(AuthResult result) {
        return AuthResponse.builder()
                .accessToken(result.getAccessToken())
                .tokenType("Bearer")
                .accessTokenExpiryMs(result.getAccessTokenExpiryMs())
                .userId(result.getUserId())
                .fullName(result.getFullName())
                .username(result.getUsername())
                .role(result.getRole())
                .build();
    }

    //Cookie Helper Methods ..........................................................
    private ResponseCookie buildRefreshCookie(String refreshToken) {
        boolean isProductionEnvironment = isProductionEnvironment();
        return ResponseCookie.from(REFRESH_COOKIE_NAME,
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8))
                .httpOnly(true)
                .secure(isProductionEnvironment)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(
                        appProperties.getSecurity()
                                .getJwt()
                                .getRefreshTokenExpiryMs()
                )).build();
    }
}
