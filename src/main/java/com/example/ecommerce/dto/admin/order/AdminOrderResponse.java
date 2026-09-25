package com.example.ecommerce.dto.admin.order;

import com.example.ecommerce.dto.order.OrderItemResponse;
import com.example.ecommerce.entity.status.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminOrderResponse(
        Long id,
        String orderNumber,
        Long userId,
        String userEmail,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingCost,
        BigDecimal total,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt
) {
}