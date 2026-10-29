package com.thewizecompany.billnova.security;


import com.thewizecompany.billnova.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final AppProperties appProperties;

    public JwtUtil(AppProperties appProperties) {
        this.appProperties = appProperties;

        this.accessTokenKey = Keys.hmacShaKeyFor(
                appProperties.getSecurity()
                        .getJwt()
                        .getAccessTokenSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );

        this.refreshTokenKey = Keys.hmacShaKeyFor(
                appProperties.getSecurity()
                        .getJwt()
                        .getRefreshTokenSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(
            UUID userId,
            String username,
            String role
    ) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(
                        now + appProperties.getSecurity().getJwt()
                                .getAccessTokenExpiryMs()
                ))
                .signWith(accessTokenKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuedAt(new Date(now))
                .expiration(new Date(
                        now + appProperties.getSecurity().getJwt().getRefreshTokenExpiryMs()
                ))
                .signWith(refreshTokenKey)
                .compact();
    }

    public String generateForgotPasswordToken(UUID userId){
        long now = System.currentTimeMillis();
        long fifteenMinutesInMillis = 15 * 60 * 1000;

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuedAt(new Date(now))
                .expiration(new Date(
                        now + fifteenMinutesInMillis
                ))
                .signWith(accessTokenKey)
                .compact();
    }

    public Claims validateAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims validateRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(refreshTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims validateForgotPasswordToken(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(Claims claims) {return claims.get("username", String.class);}

    public String extractRole(Claims claims) {return claims.get("role", String.class);}

    public long getRemainingValidityMs(Claims claims) {
        long expiry = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        return Math.max(0, expiry - now);
    }
}
