package com.thewizecompany.billnova.controller;


import com.thewizecompany.billnova.dto.CreateInvoiceRequest;
import com.thewizecompany.billnova.dto.InvoiceResponse;
import com.thewizecompany.billnova.dto.UpdateInvoiceRequest;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.responses.PageResponse;
import com.thewizecompany.billnova.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.PutExchange;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody CreateInvoiceRequest request
            ){
        return ResponseEntity.ok(
                ApiResponse.ok(
                        invoiceService.create(request), "Invoice Created"
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(
            @PathVariable String id
    ){
        return ResponseEntity.ok(
                ApiResponse.ok(
                        invoiceService.getById(id)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        invoiceService.getAll(
                                search, status, clientId, projectId, pageable
                        )
                )
        );
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceRequest request
    ){
        return ResponseEntity.ok(
                ApiResponse.ok(
                        invoiceService.update(id, request), "Invoice Updated"
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id
    ){
        invoiceService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Invoice Deleted")
        );
    }

    @PutExchange("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markLost(
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(
                ApiResponse.ok(
                        invoiceService.markLost(), "Invoice Status Updated"
                )
        );
    }


}
