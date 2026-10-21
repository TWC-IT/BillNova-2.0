package com.thewizecompany.billnova.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank (message = "Username is required")
    private String username;
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 128,
            message = "password must be between 8 and 128 characters"
    )
    private String password;
}
