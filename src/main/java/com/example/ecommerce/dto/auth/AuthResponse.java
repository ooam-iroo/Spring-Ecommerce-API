package com.example.ecommerce.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
}