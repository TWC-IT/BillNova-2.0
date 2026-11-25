package com.thewizecompany.billnova.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClientRequest {

    private  String name;
    @Email
    private String email;
    private String billingAddress;
}
