package com.example.ecommerce.dto.admin.product;

import com.example.ecommerce.entity.status.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(

        @NotNull(message = "Status is required")
        ProductStatus status
) {
}