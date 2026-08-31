package com.example.ecommerce.dto.cart;

import jakarta.validation.constraints.Min;

public record CartItemUpdateRequest(

        @Min(1)
        int quantity
) {
}