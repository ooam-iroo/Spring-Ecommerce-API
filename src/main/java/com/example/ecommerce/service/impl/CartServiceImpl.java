package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.cart.CartItemAddRequest;
import com.example.ecommerce.dto.cart.CartItemResponse;
import com.example.ecommerce.dto.cart.CartItemUpdateRequest;
import com.example.ecommerce.dto.cart.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        Cart cart = getOrCreateCart();

        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(CartItemAddRequest request) {

        Cart cart = getOrCreateCart();

        Product product = findProduct(request.productId());

        validateProduct(product);

        cart.addItem(
                product,
                request.quantity()
        );

        return toResponse(cart);
    }

    @Override
    public CartResponse updateItem(
            Long productId,
            CartItemUpdateRequest request
    ) {
        Cart cart = getOrCreateCart();

        cart.updateItemQuantity(
                productId,
                request.quantity()
        );

        return toResponse(cart);
    }

    @Override
    public void removeItem(Long productId) {

        Cart cart = getOrCreateCart();

        cart.removeItem(productId);
    }

    @Override
    public void clearCart() {

        Cart cart = getOrCreateCart();

        cart.clear();
    }

    private Cart getOrCreateCart() {

        Long userId = getCurrentUserId();

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "User not found: " + userId
                                    )
                            );

                    return cartRepository.save(
                            new Cart(user)
                    );
                });
    }

    private Long getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new BusinessException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + email
                        )
                )
                .getId();
    }

    private Product findProduct(Long productId) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + productId
                        )
                );
    }

    private void validateProduct(Product product) {

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException(
                    "Product is not available: "
                            + product.getId()
            );
        }
    }

    private CartResponse toResponse(Cart cart) {

        var items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new CartResponse(
                cart.getId(),
                items,
                total
        );
    }

    private CartItemResponse toItemResponse(
            CartItem item
    ) {

        Product product = item.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(
                        BigDecimal.valueOf(item.getQuantity())
                );

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                item.getQuantity(),
                subtotal
        );
    }
}