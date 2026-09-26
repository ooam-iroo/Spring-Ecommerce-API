package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.dashboard.AdminDashboardResponse;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.AdminDashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    @Test
    void shouldReturnDashboardStatistics() {
        when(userRepository.count()).thenReturn(10L);
        when(productRepository.count()).thenReturn(25L);
        when(categoryRepository.count()).thenReturn(5L);
        when(orderRepository.count()).thenReturn(50L);

        when(orderRepository.calculateTotalRevenue())
                .thenReturn(new BigDecimal("12500.00"));

        when(orderRepository.countByStatus(OrderStatus.PENDING))
                .thenReturn(8L);

        when(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .thenReturn(30L);

        AdminDashboardResponse result =
                adminDashboardService.getDashboard();

        assertNotNull(result);

        assertEquals(10L, result.totalUsers());
        assertEquals(25L, result.totalProducts());
        assertEquals(5L, result.totalCategories());
        assertEquals(50L, result.totalOrders());
        assertEquals(
                new BigDecimal("12500.00"),
                result.totalRevenue()
        );
        assertEquals(8L, result.pendingOrders());
        assertEquals(30L, result.completedOrders());

        verify(userRepository).count();
        verify(productRepository).count();
        verify(categoryRepository).count();
        verify(orderRepository).count();
        verify(orderRepository).calculateTotalRevenue();
        verify(orderRepository).countByStatus(OrderStatus.PENDING);
        verify(orderRepository).countByStatus(OrderStatus.DELIVERED);
    }

    @Test
    void shouldReturnZeroStatisticsWhenStoreIsEmpty() {
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);
        when(categoryRepository.count()).thenReturn(0L);
        when(orderRepository.count()).thenReturn(0L);

        when(orderRepository.calculateTotalRevenue())
                .thenReturn(BigDecimal.ZERO);

        when(orderRepository.countByStatus(OrderStatus.PENDING))
                .thenReturn(0L);

        when(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .thenReturn(0L);

        AdminDashboardResponse result =
                adminDashboardService.getDashboard();

        assertNotNull(result);

        assertEquals(0L, result.totalUsers());
        assertEquals(0L, result.totalProducts());
        assertEquals(0L, result.totalCategories());
        assertEquals(0L, result.totalOrders());
        assertEquals(BigDecimal.ZERO, result.totalRevenue());
        assertEquals(0L, result.pendingOrders());
        assertEquals(0L, result.completedOrders());
    }
}