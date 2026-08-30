package com.example.ecommerce.dto.product;

import com.example.ecommerce.entity.status.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(

        @NotNull
        Long categoryId,

        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 280)
        String slug,

        String description,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal price,

        @NotBlank
        @Size(max = 100)
        String sku,

        @NotNull
        ProductStatus status
) {
}