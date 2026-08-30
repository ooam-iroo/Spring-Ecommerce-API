package com.example.ecommerce.dto.category;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Long parentId
) {
}