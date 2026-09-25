package com.example.ecommerce.dto.admin.user;

import com.example.ecommerce.entity.status.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "Status is required")
        UserStatus status
) {
}