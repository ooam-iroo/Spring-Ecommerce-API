package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.PaymentStatus;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.entity.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User user;
    private User anotherUser;
    private Order order;

    @BeforeEach
    void setUp() {

        user = userRepository.save(
                new User(
                        "Amir",
                        "Hassani",
                        "amir@example.com",
                        passwordEncoder.encode("password"),
                        "09120000000",
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );

        anotherUser = userRepository.save(
                new User(
                        "Other",
                        "User",
                        "other@example.com",
                        passwordEncoder.encode("password"),
                        "09121111111",
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );

        Category category = categoryRepository.save(
                new Category(
                        "Electronics",
                        "electronics",
                        null
                )
        );

        Product product = productRepository.save(
                new Product(
                        category,
                        "Laptop",
                        "laptop",
                        "Test laptop",
                        new BigDecimal("1000.00"),
                        "SKU-001",
                        ProductStatus.ACTIVE
                )
        );

        order = orderRepository.save(
                new Order(
                        "ORD-TEST123456",
                        user,
                        new BigDecimal("2000.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("50.00")
                )
        );

        Cart cart = new Cart(user);
        cartRepository.save(cart);
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post("/api/v1/orders/{orderId}/payment", order.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId")
                        .value(order.getId().intValue()))
                .andExpect(jsonPath("$.orderNumber")
                        .value("ORD-TEST123456"))
                .andExpect(jsonPath("$.amount")
                        .value(1950.00))
                .andExpect(jsonPath("$.status")
                        .value("SUCCESS"))
                .andExpect(jsonPath("$.transactionReference")
                        .isNotEmpty());
    }

    @Test
    void shouldGetPaymentSuccessfully() throws Exception {

        Payment payment = paymentRepository.save(
                new Payment(
                        order,
                        order.getTotal(),
                        PaymentStatus.SUCCESS,
                        "TXN-TEST-123"
                )
        );

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        get("/api/v1/orders/{orderId}/payment", order.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId")
                        .value(order.getId().intValue()))
                .andExpect(jsonPath("$.amount")
                        .value(1950.00))
                .andExpect(jsonPath("$.status")
                        .value("SUCCESS"))
                .andExpect(jsonPath("$.transactionReference")
                        .value("TXN-TEST-123"));
    }

    @Test
    void shouldRefundPaymentSuccessfully() throws Exception {

        paymentRepository.save(
                new Payment(
                        order,
                        order.getTotal(),
                        PaymentStatus.SUCCESS,
                        "TXN-REFUND-123"
                )
        );

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment/refund",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("REFUNDED"))
                .andExpect(jsonPath("$.transactionReference")
                        .value("TXN-REFUND-123"));
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment",
                                order.getId()
                        )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenUserDoesNotOwnOrder()
            throws Exception {

        String token = jwtService.generateToken(
                anotherUser.getEmail()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Business Error"));
    }

    @Test
    void shouldReturnBadRequestWhenOrderIsCancelled()
            throws Exception {

        order.cancel();
        orderRepository.save(order);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Business Error"));
    }

    @Test
    void shouldReturnBadRequestWhenPaymentAlreadyExists()
            throws Exception {

        paymentRepository.save(
                new Payment(
                        order,
                        order.getTotal(),
                        PaymentStatus.SUCCESS,
                        "TXN-EXISTING-123"
                )
        );

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Business Error"));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist()
            throws Exception {

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment",
                                999999L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Resource Not Found"));
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExist()
            throws Exception {

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        get(
                                "/api/v1/orders/{orderId}/payment",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Resource Not Found"));
    }

    @Test
    void shouldNotAllowAnotherUserToViewPayment()
            throws Exception {

        paymentRepository.save(
                new Payment(
                        order,
                        order.getTotal(),
                        PaymentStatus.SUCCESS,
                        "TXN-PRIVATE-123"
                )
        );

        String token = jwtService.generateToken(
                anotherUser.getEmail()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/orders/{orderId}/payment",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Business Error"));
    }

    @Test
    void shouldReturnBadRequestWhenRefundingFailedPayment()
            throws Exception {

        paymentRepository.save(
                new Payment(
                        order,
                        order.getTotal(),
                        PaymentStatus.FAILED,
                        "TXN-FAILED-123"
                )
        );

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment/refund",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Business Error"));
    }

    @Test
    void shouldReturnNotFoundWhenRefundPaymentDoesNotExist()
            throws Exception {

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post(
                                "/api/v1/orders/{orderId}/payment/refund",
                                order.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Resource Not Found"));
    }
}