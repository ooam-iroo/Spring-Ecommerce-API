package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.order.OrderItemResponse;
import com.example.ecommerce.dto.order.OrderResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    public OrderResponse createOrder() {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for user: " + user.getId()
                        )
                );

        if (cart.getItems().isEmpty()) {
            throw new BusinessException(
                    "Cannot create order from an empty cart"
            );
        }

        BigDecimal subtotal = cart.getItems()
                .stream()
                .map(this::calculateItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal shippingCost = BigDecimal.ZERO;

        Order order = new Order(
                generateOrderNumber(),
                user,
                subtotal,
                discount,
                shippingCost
        );

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getName(),
                    product.getSku(),
                    product.getPrice(),
                    cartItem.getQuantity()
            );

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        cart.clear();

        return toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {

        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + id
                        )
                );

        validateOrderAccess(order, currentUser);

        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findMyOrders(Pageable pageable) {

        Long userId = getCurrentUser().getId();

        return orderRepository
                .findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public OrderResponse cancelOrder(Long id) {

        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + id
                        )
                );

        validateOrderAccess(order, currentUser);

        try {
            order.cancel();
        } catch (IllegalStateException exception) {
            throw new BusinessException(
                    exception.getMessage()
            );
        }

        return toResponse(order);
    }

    private BigDecimal calculateItemSubtotal(CartItem item) {

        return item.getProduct()
                .getPrice()
                .multiply(
                        BigDecimal.valueOf(item.getQuantity())
                );
    }

    private String generateOrderNumber() {

        return "ORD-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();
    }

    private User getCurrentUser() {

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
                );
    }

    private void validateOrderAccess(
            Order order,
            User currentUser
    ) {

        boolean isAdmin = currentUser.getRole() != null
                && currentUser.getRole().name().equals("ADMIN");

        boolean isOwner = order.getUser()
                .getId()
                .equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new BusinessException(
                    "You do not have access to this order"
            );
        }
    }

    private OrderResponse toResponse(Order order) {

        var items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getShippingCost(),
                order.getTotal(),
                items,
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toItemResponse(
            OrderItem item
    ) {

        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProductName(),
                item.getSku(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}