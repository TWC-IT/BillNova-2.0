package com.thewizecompany.billnova.controller;

import com.thewizecompany.billnova.dto.PaymentRequest;
import com.thewizecompany.billnova.dto.PaymentResponse;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @Valid @RequestBody PaymentRequest request
    ){
        PaymentResponse response = paymentService.recordPayment(request);

        return ResponseEntity.ok(
                ApiResponse.ok(response, "Payment recorded successfully")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id
    ){
        paymentService.deletePayment(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Payment deleted successfully")
        );
    }
}
