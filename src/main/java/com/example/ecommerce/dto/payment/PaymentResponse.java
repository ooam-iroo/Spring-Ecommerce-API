package com.example.ecommerce.dto.payment;

import com.example.ecommerce.entity.status.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String orderNumber,
        BigDecimal amount,
        PaymentStatus status,
        String transactionReference,
        OffsetDateTime createdAt
) {
}