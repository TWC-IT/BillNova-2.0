package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.config.AppProperties;
import com.thewizecompany.billnova.dto.AuthResult;
import com.thewizecompany.billnova.dto.LoginRequest;
import com.thewizecompany.billnova.entity.RefreshToken;
import com.thewizecompany.billnova.entity.Users;
import com.thewizecompany.billnova.repository.RefreshTokenRepository;
import com.thewizecompany.billnova.repository.UserRepository;
import com.thewizecompany.billnova.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public AuthResult login(LoginRequest loginRequest, String iPAddress, String deviceInfo) {
        String username = loginRequest.getUsername().trim();


        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials");
        } catch (LockedException e) {
            log.warn("login attempt on locked account: {} from IP: {}", username, iPAddress);

            throw new LockedException("Account is temporarily locked due to multiple failed login attempts");
        } catch (DisabledException e) {
            log.warn("login attempt on deleted account: {} from IP: {}", username, iPAddress);
            throw new DisabledException("Account is disable please contact Admin(itsupport@thewizecompany.com");
        } catch (AuthenticationException e) {
            userRepository.findByUsernameAndIsDeletedFalseAndIsActiveTrue(username)
                    .ifPresent(usr -> {
                        usr.recordFailedLogin();
                        userRepository.save(usr);
                        log.warn("Failed login attempt {}/5 for: {}", usr.getFailedLoginAttempts(), username);
                    });

            throw new BadCredentialsException("Invalid credentials");
        }

        Users user = userRepository.findByUsernameAndIsDeletedFalseAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("username not found"));

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
                .ipAddress(iPAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Created Refresh Token for user: {}", username);

        try{
//            userRepository.updateLoginSuccess(
//                    user.getId()
//            );
            user.recordSuccessfulLogin();
        } catch (Exception e){
            log.error("Update Login Success failed for user: {}, Auth will proceed. Underlying Exception: {}", username, e.getMessage());
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
}
