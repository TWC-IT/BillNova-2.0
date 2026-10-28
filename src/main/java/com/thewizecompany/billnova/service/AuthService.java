package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.config.AppProperties;
import com.thewizecompany.billnova.dto.*;
import com.thewizecompany.billnova.entity.RefreshToken;
import com.thewizecompany.billnova.entity.Users;
import com.thewizecompany.billnova.exception.BusinessException;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.RefreshTokenRepository;
import com.thewizecompany.billnova.repository.UserRepository;
import com.thewizecompany.billnova.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    @Autowired
    private JavaMailSender mailSender;




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


    @Transactional
    public void changePassword(ChangePasswordRequest request, String username){
        if(!request.getNewPassword().equals(request.getConfirmNewPassword())){
            throw new BusinessException(
                    "Passwords mismatch"
            );
        }
        if(request.getNewPassword().equals(request.getCurrentPassword())){
            throw new BusinessException(
                    "New password cannot be same as the current password"
            );
        }
        Users user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        if(!user.isActive()){
            throw new DisabledException("Account has been deactivated");
        }

        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())){
            throw new BusinessException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.saveAndFlush(user);
        refreshTokenRepository.revokeAllForUser(user.getId());

        log.info("Password changed successfully for  user: {}", user.getUsername());
    }

    public void forgotPassword(ForgotPasswordRequest request){
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);
        Users user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUsername()));

        if(!user.isActive()){
            throw new DisabledException("Account has been deactivated");
        }

        String recoverToken = jwtUtil.generateForgotPasswordToken(user.getId());
        String recoverTokenHash = hashToken(recoverToken);

        String resetUrl = appProperties.getPasswordResetUrl() + "?token=" + recoverTokenHash;

        String subject = "Password Reset Request for User: " +  user.getUsername();
        String body = """
                          <!DOCTYPE html>
                          <html lang="en">
                           <head>
                                 <meta charset="UTF-8">
                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                  <title>BillNova Password Reset</title>
                           </head>
                            <body style="margin:0;padding:0;background-color:#f3f5fb;font-family:Arial,Helvetica,sans-serif;color:#19243b;">
               \s
                            <div style="display:none;max-height:0;overflow:hidden;opacity:0;">
                                A password reset request requires administrator attention.
                            </div>
               \s
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                                        border="0" style="background-color:#f3f5fb;">
                                 <tr>
                                     <td align="center" style="padding:32px 12px;">
               \s
                                        <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                                                border="0" style="width:100%%;max-width:600px;">
               \s
                                                <!-- BillNova branding -->
                                                 <tr>
                                                    <td style="padding:0 0 22px 0;">
                                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                                              <tr>
                                                                  <td align="center" valign="middle"
                                                                      style="width:44px;height:44px;border-radius:12px;background-color:#5264e8;color:#ffffff;font-size:25px;font-weight:bold;"> B
                                                                  </td>
                                                                  <td style="padding-left:12px;">
                                                                     <div style="font-size:23px;font-weight:bold;letter-spacing:-0.5px;color:#19243b;">
                                                                          BillNova
                                                                     </div>
            \s
                                                                  </td>
                                                              </tr>
                                                        </table>
                                                    </td>
                                                 </tr>
               \s
                            <!-- Main notification card -->
                            <tr>
                                <td style="background-color:#ffffff;border:1px solid #e5e9f3;border-radius:16px;">
               \s
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                           <tr>
                                               <td style="height:5px;background-color:#5264e8;font-size:0;line-height:0;border-radius:16px 16px 0 0;">
                                                   &nbsp;
                                               </td>
                                           </tr>
                                           <tr>
                                               <td style="padding:32px 28px;">
               \s
                                                   <div style="display:inline-block;padding:7px 11px;border-radius:6px;background-color:#fff5e5;color:#9b6718;font-size:10px;font-weight:bold;letter-spacing:1px;">
                                                        ADMIN ACTION REQUIRED
                                                   </div>
               \s
                                                   <h1 style="margin:20px 0 12px;font-size:27px;line-height:35px;letter-spacing:-0.6px;color:#19243b;">
                                                       Password reset requested
                                                   </h1>
               \s
                                                   <p style="margin:0;font-size:14px;line-height:24px;color:#69758c;">
                                                      A password reset request has been initiated for a BillNova account.
                                                       Review the account details below and use the secure link to set a new password.
                                                   </p>
               \s
                                                   <!-- Account information -->
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                                   border="0" style="margin-top:24px;background-color:#f8f9fd;border:1px solid #e7eaf3;border-radius:10px;">
               \s
                                   <tr>
                                       <td colspan="2" style="padding:17px 16px 10px;font-size:10px;font-weight:bold;letter-spacing:1.3px;color:#8791a6;">
                                             ACCOUNT DETAILS
                                       </td>
                                   </tr>
               \s
                                    <tr>
                                         <td style="padding:10px 16px;font-size:13px;color:#748097;">
                                               Username
                                         </td>
                                         <td align="right" style="padding:10px 16px;font-size:13px;font-weight:bold;color:#26334d;word-break:break-word;">
                                              %s
                                         </td>
                                         </tr>
               \s
                                         <tr>
                                             <td colspan="2" style="padding:0 16px;">
                                                 <div style="height:1px;background-color:#e7eaf3;"></div>
                                             </td>
                                         </tr>
               \s
                                         <tr>
                                            <td style="padding:12px 16px;font-size:13px;color:#748097;">
                                                User ID
                                            </td>
                                         <td align="right" style="padding:12px 16px;font-size:12px;color:#45516a;word-break:break-all;">
                                              %s
                                          </td>
                                         </tr>
               \s
                                          <tr>
                                              <td colspan="2" style="padding:0 16px;">
                                                  <div style="height:1px;background-color:#e7eaf3;"></div>
                                              </td>
                                          </tr>
               \s
                                          <tr>
                                               <td style="padding:12px 16px 16px;font-size:13px;color:#748097;">
                                                    Requested at
                                               </td>
                                               <td align="right" style="padding:12px 16px 16px;font-size:12px;color:#45516a;word-break:break-word;">
                                                   %s
                                               </td>
                                          </tr>
                </table>
               \s
                            <p style="margin:25px 0 17px;font-size:14px;line-height:23px;color:#45516a;">
                               Click the button below to open the administrator password reset page.
                            </p>
               \s
                            <!-- Primary action button -->
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                       <tr>
                           <td align="center" bgcolor="#5264e8" style="border-radius:9px;">
                               <a href="%s" target="_blank"
                                   style="display:block;padding:15px 18px;border:1px solid #5264e8;border-radius:9px;color:#ffffff;text-decoration:none;font-size:14px;font-weight:bold;line-height:20px;text-align:center;">
                                         Open Password Reset Page &rarr;
                               </a>
                           </td>
                       </tr>
                </table>
               \s
                        <!-- Expiry warning -->
             <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                    border="0" style="margin-top:22px;background-color:#fff8e9;border:1px solid #f3e4bd;border-radius:9px;">
                    <tr>
                        <td style="padding:13px 14px;font-size:12px;line-height:21px;color:#805e20;">
                            <strong>Time-sensitive security link</strong><br>
                                    This reset link expires in <strong>15 minutes</strong>.
                                    Complete the password reset before it expires.
                        </td>
                    </tr>
    </table>
                   \s
               \s
    </td>
  </tr>
                                                                                                                                                                                              </table>
    </td>                                                                               \s
  </tr>                                                                             \s
               \s
        <!-- Email footer -->
             <tr>
             <td align="center" style="padding:24px 12px 0;">
             <p style="margin:0;font-size:12px;line-height:20px;color:#8995aa;">
                 Secure account management by <strong style="color:#53617a;">The Wize Company</strong>
             </p>
                   <p style="margin:7px 0 0;font-size:11px;line-height:18px;color:#a0a9b9;">
                       This is an automated security notification. Please do not reply.
                   </p>
        </td>
    </tr>
               \s
 </table>
    </td>
         </tr>
 </table>
  </body>
  </html>
 \s""".formatted(user.getUsername(), user.getId(), Instant.now(), resetUrl);

        try{
            sendHtmlEmail(appProperties.getResetEmail(), subject, body);
        }catch (Exception e){
            throw new RuntimeException("Failed to send notification email to IT Admin", e);
        }
    }


    @Transactional
    public void updatePassword(UpdatePasswordRequest request, String username){
        Users user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(()-> new ResourceNotFoundException("User not found", username));

        if(!request.getNewPassword().equals(request.getConfirmNewPassword())){
            throw new BusinessException("Password Mismatch");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

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

    private void sendHtmlEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }
}
