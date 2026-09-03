package com.example.ecommerce.service;

import com.example.ecommerce.dto.order.OrderResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {

        user = mock(User.class);

        product = mock(Product.class);

        cart = mock(Cart.class);

        cartItem = mock(CartItem.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateOrderFromCart() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        when(cartItem.getProduct())
                .thenReturn(product);

        when(cartItem.getQuantity())
                .thenReturn(2);

        when(product.getId())
                .thenReturn(100L);

        when(product.getName())
                .thenReturn("Laptop");

        when(product.getSku())
                .thenReturn("LAPTOP-001");

        when(product.getPrice())
                .thenReturn(new BigDecimal("1500.00"));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response =
                orderService.createOrder();

        assertNotNull(response);

        assertNotNull(response.orderNumber());

        assertEquals(
                OrderStatus.PENDING,
                response.status()
        );

        assertEquals(
                new BigDecimal("3000.00"),
                response.subtotal()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.discount()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.shippingCost()
        );

        assertEquals(
                new BigDecimal("3000.00"),
                response.total()
        );

        assertEquals(
                1,
                response.items().size()
        );

        verify(orderRepository)
                .save(any(Order.class));

        verify(cart)
                .clear();
    }

    @Test
    void shouldCreateOrderItemWithProductSnapshot() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        when(cartItem.getProduct())
                .thenReturn(product);

        when(cartItem.getQuantity())
                .thenReturn(2);

        when(product.getId())
                .thenReturn(100L);

        when(product.getName())
                .thenReturn("Laptop");

        when(product.getSku())
                .thenReturn("LAPTOP-001");

        when(product.getPrice())
                .thenReturn(new BigDecimal("1500.00"));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response =
                orderService.createOrder();

        var item = response.items().get(0);

        assertEquals(
                100L,
                item.productId()
        );

        assertEquals(
                "Laptop",
                item.productName()
        );

        assertEquals(
                "LAPTOP-001",
                item.sku()
        );

        assertEquals(
                new BigDecimal("1500.00"),
                item.unitPrice()
        );

        assertEquals(
                2,
                item.quantity()
        );

        assertEquals(
                new BigDecimal("3000.00"),
                item.subtotal()
        );
    }

    @Test
    void shouldClearCartAfterCreatingOrder() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        when(cartItem.getProduct())
                .thenReturn(product);

        when(cartItem.getQuantity())
                .thenReturn(1);

        when(product.getPrice())
                .thenReturn(new BigDecimal("100.00"));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder();

        verify(cart, times(1))
                .clear();
    }

    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> orderService.createOrder()
                );

        assertEquals(
                "Cannot create order from an empty cart",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(cart, never())
                .clear();
    }

    @Test
    void shouldThrowExceptionWhenCartDoesNotExist() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> orderService.createOrder()
                );

        assertEquals(
                "Cart not found for user: 1",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldFindOwnOrder() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        Order order = mock(Order.class);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(order.getUser())
                .thenReturn(user);

        when(order.getId())
                .thenReturn(1L);

        when(order.getOrderNumber())
                .thenReturn("ORD-ABC123");

        when(order.getStatus())
                .thenReturn(OrderStatus.PENDING);

        when(order.getSubtotal())
                .thenReturn(new BigDecimal("3000.00"));

        when(order.getDiscount())
                .thenReturn(BigDecimal.ZERO);

        when(order.getShippingCost())
                .thenReturn(BigDecimal.ZERO);

        when(order.getTotal())
                .thenReturn(new BigDecimal("3000.00"));

        when(order.getItems())
                .thenReturn(List.of());

        OrderResponse response =
                orderService.findById(1L);

        assertNotNull(response);

        assertEquals(
                1L,
                response.id()
        );

        assertEquals(
                "ORD-ABC123",
                response.orderNumber()
        );

        assertEquals(
                OrderStatus.PENDING,
                response.status()
        );
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> orderService.findById(999L)
                );

        assertEquals(
                "Order not found: 999",
                exception.getMessage()
        );
    }

    @Test
    void shouldPreventUserFromAccessingAnotherUsersOrder() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        User anotherUser = mock(User.class);

        when(anotherUser.getId())
                .thenReturn(2L);

        Order order = mock(Order.class);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(order.getUser())
                .thenReturn(anotherUser);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> orderService.findById(1L)
                );

        assertEquals(
                "You do not have access to this order",
                exception.getMessage()
        );
    }

    @Test
    void shouldFindMyOrders() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        prepareOrder(
                order1,
                1L,
                "ORD-111111"
        );

        prepareOrder(
                order2,
                2L,
                "ORD-222222"
        );

        Page<Order> orderPage =
                new PageImpl<>(
                        List.of(order1, order2)
                );

        PageRequest pageable =
                PageRequest.of(0, 10);

        when(orderRepository.findByUserId(
                1L,
                pageable
        )).thenReturn(orderPage);

        Page<OrderResponse> response =
                orderService.findMyOrders(pageable);

        assertNotNull(response);

        assertEquals(
                2,
                response.getTotalElements()
        );

        assertEquals(
                2,
                response.getContent().size()
        );

        verify(orderRepository)
                .findByUserId(1L, pageable);
    }

    @Test
    void shouldCancelPendingOrder() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        Order order = mock(Order.class);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(order.getUser())
                .thenReturn(user);

        when(order.getId())
                .thenReturn(1L);

        when(order.getOrderNumber())
                .thenReturn("ORD-CANCEL01");

        when(order.getStatus())
                .thenReturn(OrderStatus.PENDING);

        when(order.getSubtotal())
                .thenReturn(new BigDecimal("100.00"));

        when(order.getDiscount())
                .thenReturn(BigDecimal.ZERO);

        when(order.getShippingCost())
                .thenReturn(BigDecimal.ZERO);

        when(order.getTotal())
                .thenReturn(new BigDecimal("100.00"));

        when(order.getItems())
                .thenReturn(List.of());

        OrderResponse response =
                orderService.cancelOrder(1L);

        verify(order)
                .cancel();

        assertNotNull(response);
    }

    @Test
    void shouldThrowBusinessExceptionWhenCancellingInvalidOrder() {

        authenticate("amir@example.com");

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(1L);

        Order order = mock(Order.class);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(order.getUser())
                .thenReturn(user);

        doThrow(
                new IllegalStateException(
                        "Order cannot be cancelled in status: SHIPPED"
                )
        ).when(order).cancel();

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> orderService.cancelOrder(1L)
                );

        assertEquals(
                "Order cannot be cancelled in status: SHIPPED",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticated() {

        SecurityContextHolder.clearContext();

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> orderService.findMyOrders(
                                PageRequest.of(0, 10)
                        )
                );

        assertEquals(
                "User is not authenticated",
                exception.getMessage()
        );
    }

    private void authenticate(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void prepareOrder(
            Order order,
            Long id,
            String orderNumber
    ) {

        when(order.getId())
                .thenReturn(id);

        when(order.getOrderNumber())
                .thenReturn(orderNumber);

        when(order.getStatus())
                .thenReturn(OrderStatus.PENDING);

        when(order.getSubtotal())
                .thenReturn(new BigDecimal("100.00"));

        when(order.getDiscount())
                .thenReturn(BigDecimal.ZERO);

        when(order.getShippingCost())
                .thenReturn(BigDecimal.ZERO);

        when(order.getTotal())
                .thenReturn(new BigDecimal("100.00"));

        when(order.getItems())
                .thenReturn(List.of());
    }
}