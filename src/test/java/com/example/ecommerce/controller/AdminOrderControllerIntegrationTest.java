package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.OrderRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User user;
    private Order order;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(
                new User(
                        "Admin",
                        "User",
                        "admin@example.com",
                        passwordEncoder.encode("password"),
                        "09120000001",
                        UserStatus.ACTIVE,
                        UserRole.ADMIN
                )
        );

        user = userRepository.save(
                new User(
                        "John",
                        "Doe",
                        "john@example.com",
                        passwordEncoder.encode("password"),
                        "09120000002",
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );

        order = orderRepository.save(
                new Order(
                        "ORD-TEST-001",
                        user,
                        new BigDecimal("100.00"),
                        new BigDecimal("10.00"),
                        new BigDecimal("15.00")
                )
        );

        adminToken = jwtService.generateToken(admin.getEmail());
        userToken = jwtService.generateToken(user.getEmail());
    }

    @Test
    void shouldAllowAdminToFindOrders() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/orders")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(order.getId()))
                .andExpect(jsonPath("$.content[0].orderNumber")
                        .value("ORD-TEST-001"))
                .andExpect(jsonPath("$.content[0].userId")
                        .value(user.getId()))
                .andExpect(jsonPath("$.content[0].userEmail")
                        .value("john@example.com"))
                .andExpect(jsonPath("$.content[0].status")
                        .value("PENDING"));
    }

    @Test
    void shouldAllowAdminToFindOrder() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/orders/" + order.getId())
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.orderNumber")
                        .value("ORD-TEST-001"))
                .andExpect(jsonPath("$.userId")
                        .value(user.getId()))
                .andExpect(jsonPath("$.userEmail")
                        .value("john@example.com"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"))
                .andExpect(jsonPath("$.subtotal")
                        .value(100.00))
                .andExpect(jsonPath("$.discount")
                        .value(10.00))
                .andExpect(jsonPath("$.shippingCost")
                        .value(15.00))
                .andExpect(jsonPath("$.total")
                        .value(105.00));
    }

    @Test
    void shouldAllowAdminToUpdateOrderStatus() throws Exception {
        String requestBody = """
                {
                    "status": "PROCESSING"
                }
                """;

        mockMvc.perform(
                        patch("/api/v1/admin/orders/" + order.getId() + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.status")
                        .value("PROCESSING"));
    }

    @Test
    void shouldReturnForbiddenForNormalUser() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/orders")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/orders")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundForNonExistingOrder() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/orders/999999")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidStatus() throws Exception {
        String requestBody = """
                {
                    "status": "INVALID_STATUS"
                }
                """;

        mockMvc.perform(
                        patch("/api/v1/admin/orders/" + order.getId() + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsMissing() throws Exception {
        String requestBody = """
                {}
                """;

        mockMvc.perform(
                        patch("/api/v1/admin/orders/" + order.getId() + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}