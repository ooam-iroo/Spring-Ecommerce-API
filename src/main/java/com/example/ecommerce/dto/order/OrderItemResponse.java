package com.example.ecommerce.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}