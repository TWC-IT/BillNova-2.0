package com.thewizecompany.billnova.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TemplateResponse {
    private final UUID id;
    private final String name;
    private final boolean isDefault;
    private final String content;
}
