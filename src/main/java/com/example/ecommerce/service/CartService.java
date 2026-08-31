package com.example.ecommerce.service;

import com.example.ecommerce.dto.cart.CartItemAddRequest;
import com.example.ecommerce.dto.cart.CartItemUpdateRequest;
import com.example.ecommerce.dto.cart.CartResponse;

public interface CartService {

    CartResponse getCart();

    CartResponse addItem(CartItemAddRequest request);

    CartResponse updateItem(
            Long productId,
            CartItemUpdateRequest request
    );

    void removeItem(Long productId);

    void clearCart();
}