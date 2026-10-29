package com.thewizecompany.billnova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "new password is required")
    @Size(min = 8, max = 128)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)" +
                    "(?=.*[!@#$%&*?])[A-Za-z\\d!@#$%&*?]{8,}$",
            message = "Password must contain at least one uppercase letter, " +
                    "one lowercase letter, one number, and one special character"
    )
    private String newPassword;
    @NotBlank(message = "Please confirm your new password")
    private String confirmNewPassword;
}
