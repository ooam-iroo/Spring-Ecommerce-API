package com.example.ecommerce.controller;

import com.example.ecommerce.dto.payment.PaymentResponse;
import com.example.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                paymentService.processPayment(orderId)
        );
    }

    @GetMapping
    public ResponseEntity<PaymentResponse> findPayment(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                paymentService.findByOrderId(orderId)
        );
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                paymentService.refundPayment(orderId)
        );
    }
}