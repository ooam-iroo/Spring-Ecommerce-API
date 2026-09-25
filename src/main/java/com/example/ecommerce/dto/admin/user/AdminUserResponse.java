package com.example.ecommerce.dto.admin.user;

import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;

import java.time.OffsetDateTime;

public record AdminUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        UserStatus status,
        UserRole role,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}