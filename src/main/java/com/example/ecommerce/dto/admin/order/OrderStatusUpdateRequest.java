package com.example.ecommerce.dto.admin.order;

import com.example.ecommerce.entity.status.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "Status is required")
        OrderStatus status
) {
}