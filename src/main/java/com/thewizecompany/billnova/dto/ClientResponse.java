package com.thewizecompany.billnova.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;


@Getter
@Builder
public class ClientResponse {
    private UUID id;
    private String name;
    private String email;
    private String billingAddress;
}
