package com.example.ecommerce.dto.order;

import com.example.ecommerce.entity.status.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingCost,
        BigDecimal total,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt
) {
}