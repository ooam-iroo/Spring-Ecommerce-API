package com.example.ecommerce.dto.review;

import java.time.OffsetDateTime;

public record ReviewResponse(
        Long id,
        Long productId,
        Long userId,
        String userName,
        Integer rating,
        String comment,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}