package com.example.ecommerce.service;

import com.example.ecommerce.dto.payment.PaymentResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.entity.status.PaymentStatus;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.payment.PaymentProvider;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentProvider paymentProvider;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private User anotherUser;
    private Category category;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {

        user = new User(
                "Amir",
                "Hassani",
                "amir@example.com",
                "password",
                "09120000000",
                UserStatus.ACTIVE,
                UserRole.USER
        );

        anotherUser = new User(
                "Other",
                "User",
                "other@example.com",
                "password",
                "09121111111",
                UserStatus.ACTIVE,
                UserRole.USER
        );

        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        category = new Category(
                "Electronics",
                "electronics",
                null
        );

        product = new Product(
                category,
                "Laptop",
                "laptop",
                "Test laptop",
                new BigDecimal("1000.00"),
                "SKU-001",
                ProductStatus.ACTIVE
        );

        order = new Order(
                "ORD-TEST123456",
                user,
                new BigDecimal("2000.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00")
        );

        authenticate("amir@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // processPayment
    // -------------------------------------------------------------------------

    @Test
    void shouldProcessPaymentSuccessfully() {

        PaymentProvider.PaymentResult providerResult =
                new PaymentProvider.PaymentResult(
                        true,
                        "TXN-123456789"
                );

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(1L))
                .thenReturn(false);

        when(paymentProvider.process(
                order.getOrderNumber(),
                order.getTotal()
        )).thenReturn(providerResult);

        Payment savedPayment = new Payment(
                order,
                order.getTotal(),
                PaymentStatus.SUCCESS,
                "TXN-123456789"
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        PaymentResponse response =
                paymentService.processPayment(1L);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(
                order.getTotal(),
                response.amount()
        );
        assertEquals(
                "TXN-123456789",
                response.transactionReference()
        );
        assertEquals(
                order.getOrderNumber(),
                response.orderNumber()
        );

        verify(paymentProvider).process(
                order.getOrderNumber(),
                order.getTotal()
        );

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldProcessPaymentAsFailedWhenProviderFails() {

        PaymentProvider.PaymentResult providerResult =
                new PaymentProvider.PaymentResult(
                        false,
                        "TXN-FAILED-123"
                );

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(1L))
                .thenReturn(false);

        when(paymentProvider.process(
                order.getOrderNumber(),
                order.getTotal()
        )).thenReturn(providerResult);

        Payment savedPayment = new Payment(
                order,
                order.getTotal(),
                PaymentStatus.FAILED,
                "TXN-FAILED-123"
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        PaymentResponse response =
                paymentService.processPayment(1L);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals(
                "TXN-FAILED-123",
                response.transactionReference()
        );

        verify(paymentProvider).process(
                order.getOrderNumber(),
                order.getTotal()
        );

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.processPayment(999L)
        );

        verify(paymentProvider, never())
                .process(anyString(), any());

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotOwnOrder() {

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        Order anotherOrder = new Order(
                "ORD-OTHER12345",
                anotherUser,
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(orderRepository.findById(2L))
                .thenReturn(Optional.of(anotherOrder));

        assertThrows(
                BusinessException.class,
                () -> paymentService.processPayment(2L)
        );

        verify(paymentProvider, never())
                .process(anyString(), any());

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderIsCancelled() {

        order.cancel();

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );

        assertThrows(
                BusinessException.class,
                () -> paymentService.processPayment(1L)
        );

        verify(paymentProvider, never())
                .process(anyString(), any());

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenPaymentAlreadyExists() {

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(1L))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> paymentService.processPayment(1L)
        );

        verify(paymentProvider, never())
                .process(anyString(), any());

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    void shouldUseOrderTotalAsPaymentAmount() {

        PaymentProvider.PaymentResult providerResult =
                new PaymentProvider.PaymentResult(
                        true,
                        "TXN-AMOUNT-123"
                );

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(1L))
                .thenReturn(false);

        when(paymentProvider.process(
                order.getOrderNumber(),
                order.getTotal()
        )).thenReturn(providerResult);

        Payment savedPayment = new Payment(
                order,
                order.getTotal(),
                PaymentStatus.SUCCESS,
                "TXN-AMOUNT-123"
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        PaymentResponse response =
                paymentService.processPayment(1L);

        assertEquals(
                new BigDecimal("1950.00"),
                response.amount()
        );

        verify(paymentProvider).process(
                order.getOrderNumber(),
                new BigDecimal("1950.00")
        );
    }

    // -------------------------------------------------------------------------
    // findByOrderId
    // -------------------------------------------------------------------------

    @Test
    void shouldFindPaymentByOrderId() {

        Payment payment = new Payment(
                order,
                order.getTotal(),
                PaymentStatus.SUCCESS,
                "TXN-123456"
        );

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(payment));

        PaymentResponse response =
                paymentService.findByOrderId(1L);

        assertNotNull(response);
        assertEquals(
                PaymentStatus.SUCCESS,
                response.status()
        );
        assertEquals(
                "TXN-123456",
                response.transactionReference()
        );
        assertEquals(
                order.getTotal(),
                response.amount()
        );
    }

    @Test
    void shouldThrowExceptionWhenPaymentDoesNotExist() {

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.findByOrderId(1L)
        );
    }

    @Test
    void shouldNotAllowUserToViewAnotherUsersPayment() {

        Order anotherOrder = new Order(
                "ORD-OTHER12345",
                anotherUser,
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(2L))
                .thenReturn(Optional.of(anotherOrder));

        assertThrows(
                BusinessException.class,
                () -> paymentService.findByOrderId(2L)
        );

        verify(paymentRepository, never())
                .findByOrderId(anyLong());
    }

    // -------------------------------------------------------------------------
    // refundPayment
    // -------------------------------------------------------------------------

    @Test
    void shouldRefundSuccessfulPayment() {

        Payment payment = new Payment(
                order,
                order.getTotal(),
                PaymentStatus.SUCCESS,
                "TXN-REFUND-123"
        );

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(payment));

        PaymentResponse response =
                paymentService.refundPayment(1L);

        assertEquals(
                PaymentStatus.REFUNDED,
                response.status()
        );

        assertEquals(
                PaymentStatus.REFUNDED,
                payment.getStatus()
        );
    }

    @Test
    void shouldThrowExceptionWhenRefundingFailedPayment() {

        Payment payment = new Payment(
                order,
                order.getTotal(),
                PaymentStatus.FAILED,
                "TXN-FAILED-123"
        );

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(payment));

        assertThrows(
                BusinessException.class,
                () -> paymentService.refundPayment(1L)
        );

        assertEquals(
                PaymentStatus.FAILED,
                payment.getStatus()
        );
    }

    @Test
    void shouldThrowExceptionWhenRefundingAlreadyRefundedPayment() {

        Payment payment = new Payment(
                order,
                order.getTotal(),
                PaymentStatus.SUCCESS,
                "TXN-REFUND-123"
        );

        payment.refund();

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(payment));

        assertThrows(
                BusinessException.class,
                () -> paymentService.refundPayment(1L)
        );

        assertEquals(
                PaymentStatus.REFUNDED,
                payment.getStatus()
        );
    }

    @Test
    void shouldThrowExceptionWhenRefundPaymentDoesNotExist() {

        when(userRepository.findByEmail("amir@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.refundPayment(1L)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticated() {

        SecurityContextHolder.clearContext();

        assertThrows(
                BusinessException.class,
                () -> paymentService.processPayment(1L)
        );

        verifyNoInteractions(
                userRepository,
                orderRepository,
                paymentRepository,
                paymentProvider
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void authenticate(String email) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}