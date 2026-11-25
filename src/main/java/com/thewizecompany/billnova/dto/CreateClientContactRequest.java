package com.thewizecompany.billnova.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateClientContactRequest {
    @NotBlank(message = "Contact name is required")
    private String name;
    @Email
    @NotBlank(message = "Contact email is required")
    private String email;
    @NotNull(message = "Client Id is required")
    private UUID clientId;
}
