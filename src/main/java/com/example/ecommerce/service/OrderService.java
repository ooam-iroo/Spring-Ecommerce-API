package com.example.ecommerce.service;

import com.example.ecommerce.dto.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder();

    OrderResponse findById(Long id);

    Page<OrderResponse> findMyOrders(Pageable pageable);

    OrderResponse cancelOrder(Long id);
}