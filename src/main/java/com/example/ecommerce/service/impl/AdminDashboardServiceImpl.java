package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.admin.dashboard.AdminDashboardResponse;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    @Override
    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalOrders = orderRepository.count();

        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();

        long pendingOrders =
                orderRepository.countByStatus(OrderStatus.PENDING);

        long completedOrders =
                orderRepository.countByStatus(OrderStatus.DELIVERED);

        return new AdminDashboardResponse(
                totalUsers,
                totalProducts,
                totalCategories,
                totalOrders,
                totalRevenue,
                pendingOrders,
                completedOrders
        );
    }
}