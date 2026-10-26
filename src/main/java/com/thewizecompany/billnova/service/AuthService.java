package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.config.AppProperties;
import com.thewizecompany.billnova.dto.AuthResult;
import com.thewizecompany.billnova.dto.LoginRequest;
import com.thewizecompany.billnova.entity.RefreshToken;
import com.thewizecompany.billnova.entity.Users;
import com.thewizecompany.billnova.repository.RefreshTokenRepository;
import com.thewizecompany.billnova.repository.UserRepository;
import com.thewizecompany.billnova.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;



    public AuthResult login(LoginRequest loginRequest, String ipAddress, String deviceInfo) {
        String username = loginRequest.getUsername().trim().toLowerCase(Locale.ROOT);


        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            loginRequest.getPassword()
                    ));
        } catch (LockedException e) {
            log.warn("login attempt on locked account: {} from IP: {}", username, ipAddress);

            throw new LockedException("Account is temporarily locked due to multiple failed login attempts");
        } catch (DisabledException e) {
            log.warn("login attempt on deleted account: {} from IP: {}", username, ipAddress);
            throw new DisabledException("Account is disable please contact Admin(itsupport@thewizecompany.com");
        } catch (AuthenticationException e) {
            // BadCredentialsException, unknown user (Spring maps it to BadCredentials), anything else.
            log.warn("Failed login for: {} from IP: {}", username, ipAddress);
            recordFailureSafely(username);
            // Same response for "wrong password" and "no such user" -> no user enumeration.
            throw new BadCredentialsException("Invalid credentials");
        }

        Users user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        String rawRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        String tokenHash = hashToken(rawRefreshToken);
        long refreshTokenExpiryMs = appProperties.getSecurity()
                .getJwt().getRefreshTokenExpiryMs();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiry(Instant.now().plusMillis(refreshTokenExpiryMs))
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Created Refresh Token for user: {}", username);

        try{
            recordSuccess(user.getId());
        } catch (Exception e){
            log.error("Recording Login Success failed for user: {}, cause: {}", username, e.getMessage());
        }

        log.info("Login Success for user: {}", username);

        return AuthResult.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .accessTokenExpiryMs(appProperties.getSecurity().getJwt().getAccessTokenExpiryMs())
                .userId(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        try{
            jwtUtil.validateAccessToken(accessToken);
        } catch (Exception e){
            log.debug("Access token invalid during logout: {}", e.getMessage());
        }
        if( refreshToken != null && !refreshToken.isBlank()){
            String tokenHash = hashToken(refreshToken);
            refreshTokenRepository.findByTokenHashAndIsRevokedFalseAndIsDeletedFalse(tokenHash)
                    .ifPresent(rt -> {
                        rt.revoke();
                        refreshTokenRepository.save(rt);
                    });
            log.info("User logged out successfully");
        }
    }

    @Transactional
    public AuthResult refresh(String refreshToken, String ipAddress, String deviceInfo) {
        Claims claims;
        try{
            claims = jwtUtil.validateRefreshToken(refreshToken);
        } catch (Exception e){
            throw new BadCredentialsException("Invalid refresh token");
        }

        String tokenHash = hashToken(refreshToken);

        log.debug("Refresh lookup - token prefix: {}", tokenHash);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHashAndIsRevokedFalseAndIsDeletedFalse(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if(storedToken.isExpired()){
            storedToken.revoke();
            refreshTokenRepository.save(storedToken);
            throw new BadCredentialsException("Refresh token expired");
        }

        Users user = userRepository.findByIdAndIsDeletedFalse(storedToken.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));

        if(!user.isActive()){
            throw new DisabledException("Account has been deactivated");
        }
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        String newRawRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        long refreshExpiryMs = appProperties.getSecurity().getJwt().getRefreshTokenExpiryMs();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(newRawRefreshToken))
                .expiry(Instant.now().plusMillis(refreshExpiryMs))
                .isRevoked(false)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo != null ? deviceInfo : storedToken.getDeviceInfo())
                .build();

        refreshTokenRepository.saveAndFlush(newRefreshToken);
        log.debug("Token refreshed for user: {}", user.getUsername());

        return AuthResult.builder()
                .accessToken(newAccessToken)
                .accessTokenExpiryMs(appProperties.getSecurity().getJwt().getAccessTokenExpiryMs())
                .refreshToken(newRawRefreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    //Helper Methods .........................................................................

    private String hashToken(String rawToken) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }

    private void recordFailureSafely(String username) {
        try{
            recordFailure(username);
        } catch (Exception e){
            log.error("Could not record failed login for {}: {}", username, e.getMessage());
        }
    }

    @Transactional
    private void recordSuccess(UUID userId) {
        userRepository.findById(userId).ifPresent(Users::recordSuccessfulLogin);
    }

    @Transactional
    private void recordFailure(String username) {
        int maxAttempts = appProperties.getSecurity().getRateLimit().getMaxLoginAttempts();
        userRepository.findByUsernameAndIsDeletedFalse(username).ifPresent(users -> {
            users.recordFailedLogin(maxAttempts, LOCK_DURATION);
            log.warn("Failed login attemp {}/{} for: {}", users.getFailedLoginAttempts(),
                    maxAttempts, username);
            if(users.isAccountLocked()){
                log.warn("Account locked until {} for: {}", users.getAccountLockedUntil(), username);
            }
        });
    }
}
