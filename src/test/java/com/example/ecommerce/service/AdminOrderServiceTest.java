package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.order.AdminOrderResponse;
import com.example.ecommerce.dto.admin.order.OrderStatusUpdateRequest;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.service.impl.AdminOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminOrderServiceImpl adminOrderService;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User(
                "John",
                "Doe",
                "john@example.com",
                "password",
                "09120000000",
                UserStatus.ACTIVE,
                UserRole.USER
        );

        ReflectionTestUtils.setField(user, "id", 10L);

        order = new Order(
                "ORD-123456789",
                user,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("15.00")
        );

        ReflectionTestUtils.setField(order, "id", 1L);
    }

    @Test
    void shouldFindAllOrders() {
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(page);

        var result = adminOrderService.findOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        AdminOrderResponse response = result.getContent().get(0);

        assertEquals(1L, response.id());
        assertEquals("ORD-123456789", response.orderNumber());
        assertEquals(10L, response.userId());
        assertEquals("john@example.com", response.userEmail());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("100.00"), response.subtotal());
        assertEquals(new BigDecimal("10.00"), response.discount());
        assertEquals(new BigDecimal("15.00"), response.shippingCost());
        assertEquals(new BigDecimal("105.00"), response.total());
        assertTrue(response.items().isEmpty());

        verify(orderRepository).findAll(pageable);
    }

    @Test
    void shouldFindOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        AdminOrderResponse result = adminOrderService.findOrder(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ORD-123456789", result.orderNumber());
        assertEquals(10L, result.userId());
        assertEquals("john@example.com", result.userEmail());
        assertEquals(OrderStatus.PENDING, result.status());
        assertEquals(new BigDecimal("105.00"), result.total());
        assertTrue(result.items().isEmpty());

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminOrderService.findOrder(999L)
        );

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldUpdateOrderStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest(OrderStatus.PROCESSING);

        AdminOrderResponse result =
                adminOrderService.updateOrderStatus(1L, request);

        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.status());
        assertEquals(OrderStatus.PROCESSING, order.getStatus());

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistingOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest(OrderStatus.PROCESSING);

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminOrderService.updateOrderStatus(999L, request)
        );

        verify(orderRepository).findById(999L);
    }
}