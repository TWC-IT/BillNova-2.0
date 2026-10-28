package com.thewizecompany.billnova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank(message = "username is required")
    private String username;
}
