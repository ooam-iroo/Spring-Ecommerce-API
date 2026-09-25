package com.example.ecommerce.dto.admin.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 180, message = "Slug must not exceed 180 characters")
        String slug,

        Long parentId
) {
}