package com.example.ecommerce.controller;

import com.example.ecommerce.dto.admin.order.AdminOrderResponse;
import com.example.ecommerce.dto.admin.order.OrderStatusUpdateRequest;
import com.example.ecommerce.service.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<Page<AdminOrderResponse>> findOrders(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(adminOrderService.findOrders(pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderResponse> findOrder(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(adminOrderService.findOrder(orderId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                adminOrderService.updateOrderStatus(orderId, request)
        );
    }
}