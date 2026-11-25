package com.thewizecompany.billnova.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClientRequest {

    @NotBlank(message = "Client name is required")
    private String name;

    @NotBlank(message = "Client email is required")
    @Email
    private String email;

    @NotBlank(message = "Client's billing address is required")
    private String billingAddress;
}
