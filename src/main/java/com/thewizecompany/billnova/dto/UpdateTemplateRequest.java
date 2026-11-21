package com.thewizecompany.billnova.dto;


import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTemplateRequest {
    @Size(min = 2 , max = 100)
    private String templateName;
    private String content;
    private boolean isDefault;
}
