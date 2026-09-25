package com.example.ecommerce.dto.admin.category;

import java.time.OffsetDateTime;

public record AdminCategoryResponse(
        Long id,
        String name,
        String slug,
        Long parentId,
        String parentName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}