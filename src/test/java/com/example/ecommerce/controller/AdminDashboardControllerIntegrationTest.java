package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminDashboardControllerIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {

        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User(
                "Admin",
                "User",
                "admin@example.com",
                passwordEncoder.encode("password"),
                "09120000001",
                UserStatus.ACTIVE,
                UserRole.ADMIN
        );

        User user = new User(
                "Normal",
                "User",
                "user@example.com",
                passwordEncoder.encode("password"),
                "09120000002",
                UserStatus.ACTIVE,
                UserRole.USER
        );

        userRepository.save(admin);
        userRepository.save(user);

        adminToken = jwtService.generateToken(admin.getEmail());
        userToken = jwtService.generateToken(user.getEmail());

        Category category = new Category(
                "Electronics",
                "electronics",
                null
        );

        categoryRepository.save(category);

        Product product = new Product(
                category,
                "Laptop",
                "laptop",
                "Test laptop",
                new BigDecimal("1000.00"),
                "SKU-001",
                ProductStatus.ACTIVE
        );

        productRepository.save(product);

        Order pendingOrder = new Order(
                "ORD-001",
                user,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("10.00")
        );

        Order deliveredOrder = new Order(
                "ORD-002",
                user,
                new BigDecimal("200.00"),
                new BigDecimal("20.00"),
                new BigDecimal("10.00")
        );
        deliveredOrder.changeStatus(OrderStatus.DELIVERED);

        Order cancelledOrder = new Order(
                "ORD-003",
                user,
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        cancelledOrder.changeStatus(OrderStatus.CANCELLED);

        orderRepository.save(pendingOrder);
        orderRepository.save(deliveredOrder);
        orderRepository.save(cancelledOrder);
    }

    @Test
    void shouldReturnDashboardStatisticsForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.totalProducts").value(1))
                .andExpect(jsonPath("$.totalCategories").value(1))
                .andExpect(jsonPath("$.totalOrders").value(3))
                .andExpect(jsonPath("$.totalRevenue").value(300.0))
                .andExpect(jsonPath("$.pendingOrders").value(1))
                .andExpect(jsonPath("$.completedOrders").value(1));
    }

    @Test
    void shouldReturnForbiddenForNormalUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}