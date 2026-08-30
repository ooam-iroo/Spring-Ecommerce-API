package com.example.ecommerce.dto.product;

import com.example.ecommerce.entity.status.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String sku,
        ProductStatus status
) {
}