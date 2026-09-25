package com.example.ecommerce.dto.admin.user;

import com.example.ecommerce.entity.status.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}