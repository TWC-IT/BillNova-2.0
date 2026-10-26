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
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppProperties appProperties;
    private final Environment environment;

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/api/v2/auth";

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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal String username,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ){
        String refreshToken = extractRefreshCookie(httpRequest);
        String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        authService.logout(accessToken, refreshToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return ResponseEntity.ok(
                ApiResponse.ok("Logout Successful")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ){
        String refreshToken = extractRefreshCookie(httpRequest);
        if(refreshToken == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(
                            "Refresh Token not found. Please login again.",
                            "REFRESH_TOKEN_MISSING"
                    ));
        }
        AuthResult result = authService.refresh(refreshToken, extractIpAddress(httpRequest), extractDeviceInfo(httpRequest));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.getRefreshToken()).toString());

        return ResponseEntity.ok(
                ApiResponse.ok(toAuthResponse(result), "Refresh Successful")
        );
    }

    //Helper Methods..........................................................

    private String extractIpAddress(HttpServletRequest httpRequest) {

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
        return environment.acceptsProfiles(Profiles.of("prod"));
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

    private String extractRefreshCookie(HttpServletRequest httpRequest) {
        if(httpRequest.getCookies() != null) {
            for(var cookie: httpRequest.getCookies()){
                if(REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isProductionEnvironment())
                .sameSite("Lax")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
