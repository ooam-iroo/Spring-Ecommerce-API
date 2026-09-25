package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.order.AdminOrderResponse;
import com.example.ecommerce.dto.admin.order.OrderStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOrderService {

    Page<AdminOrderResponse> findOrders(Pageable pageable);

    AdminOrderResponse findOrder(Long orderId);

    AdminOrderResponse updateOrderStatus(
            Long orderId,
            OrderStatusUpdateRequest request
    );
}