package com.thewizecompany.billnova.controller;


import com.thewizecompany.billnova.dto.ChangeOrderResponse;
import com.thewizecompany.billnova.dto.CreateChangeOrderRequest;
import com.thewizecompany.billnova.dto.UpdateChangeOrderRequest;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.responses.PageResponse;
import com.thewizecompany.billnova.service.ChangeOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/changeorders")
@RequiredArgsConstructor
public class ChangeOrderController {
    private final ChangeOrderService changeOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChangeOrderResponse>> create(
            @Valid @RequestBody CreateChangeOrderRequest request
    ){
        ChangeOrderResponse response = changeOrderService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Change Order Created"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id
    ){
        changeOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Change Order Deleted"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ChangeOrderResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateChangeOrderRequest request
    ){
        ChangeOrderResponse response = changeOrderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Change Order Updated"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PageResponse<ChangeOrderResponse>>> findAll(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ){
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        changeOrderService.getAllByProjectId(pageable, id)
                )
        );
    }
}
