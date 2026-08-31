package com.example.ecommerce.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemAddRequest(

        @NotNull
        Long productId,

        @Min(1)
        int quantity
) {
}