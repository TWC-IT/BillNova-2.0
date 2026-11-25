package com.thewizecompany.billnova.controller;


import com.thewizecompany.billnova.dto.CreateTemplateRequest;
import com.thewizecompany.billnova.dto.TemplateResponse;
import com.thewizecompany.billnova.dto.UpdateTemplateRequest;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/templates")
@RequiredArgsConstructor
public class TemplateController {
    private TemplateService templateService;

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> create(
            @Valid @RequestBody CreateTemplateRequest request
            ){
        TemplateResponse response = templateService.create(request);

        return ResponseEntity.ok(
                ApiResponse.ok(response, "Template created successfully")
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTemplateRequest request
    ){
        TemplateResponse response = templateService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.ok(response, "Template updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id
    ){
        templateService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Template deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getAll(){
        return ResponseEntity.ok(
                ApiResponse.ok(templateService.getAll())
        );
    }

}
