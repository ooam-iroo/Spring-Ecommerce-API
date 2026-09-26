package com.example.ecommerce.dto.admin.dashboard;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        long totalUsers,
        long totalProducts,
        long totalCategories,
        long totalOrders,
        BigDecimal totalRevenue,
        long pendingOrders,
        long completedOrders
) {
}