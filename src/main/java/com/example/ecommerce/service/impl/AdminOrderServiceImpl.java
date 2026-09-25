package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.admin.order.AdminOrderResponse;
import com.example.ecommerce.dto.admin.order.OrderStatusUpdateRequest;
import com.example.ecommerce.dto.order.OrderItemResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> findOrders(Pageable pageable) {
        return orderRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponse findOrder(Long orderId) {
        return toResponse(findOrderEntity(orderId));
    }

    @Override
    public AdminOrderResponse updateOrderStatus(
            Long orderId,
            OrderStatusUpdateRequest request
    ) {
        Order order = findOrderEntity(orderId);

        order.changeStatus(request.status());

        return toResponse(order);
    }

    private Order findOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        )
                );
    }

    private AdminOrderResponse toResponse(Order order) {

        var items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new AdminOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getShippingCost(),
                order.getTotal(),
                items,
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {

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