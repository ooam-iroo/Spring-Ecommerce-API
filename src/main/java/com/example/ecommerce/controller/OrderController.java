package com.example.ecommerce.controller;

import com.example.ecommerce.dto.order.OrderResponse;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder() {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder());
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> findMyOrders(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                orderService.findMyOrders(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                orderService.findById(id)
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                orderService.cancelOrder(id)
        );
    }
}