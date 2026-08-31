package com.example.ecommerce.dto.product;

import com.example.ecommerce.entity.status.ProductStatus;

import java.math.BigDecimal;

public record ProductFilterRequest(
        String name,
        Long categoryId,
        ProductStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}