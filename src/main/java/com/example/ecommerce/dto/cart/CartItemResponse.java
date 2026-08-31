package com.example.ecommerce.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        String sku,
        BigDecimal price,
        int quantity,
        BigDecimal subtotal
) {
}