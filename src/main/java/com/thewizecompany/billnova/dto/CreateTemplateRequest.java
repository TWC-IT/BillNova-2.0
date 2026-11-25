package com.thewizecompany.billnova.dto;


import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTemplateRequest {
    @NotBlank(message = "template name is required")
    @Size(min = 2 , max = 100)
    private String templateName;
    @NotBlank(message = "template content is required")
    private String content;
    private boolean setAsDefault = false;
}
