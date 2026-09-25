package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.CategoryRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private User normalUser;
    private Product product;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {

        adminUser = userRepository.save(
                new User(
                        "Admin",
                        "User",
                        "admin-product@example.com",
                        passwordEncoder.encode("password"),
                        "09120000011",
                        UserStatus.ACTIVE,
                        UserRole.ADMIN
                )
        );

        normalUser = userRepository.save(
                new User(
                        "Normal",
                        "User",
                        "user-product@example.com",
                        passwordEncoder.encode("password"),
                        "09120000012",
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );

        Category category = categoryRepository.save(
                new Category(
                        "Electronics",
                        "electronics-admin",
                        null
                )
        );

        product = productRepository.save(
                new Product(
                        category,
                        "Admin Product",
                        "admin-product",
                        "Product for admin tests",
                        new BigDecimal("999.99"),
                        "ADMIN-PRODUCT-001",
                        ProductStatus.ACTIVE
                )
        );

        adminToken = jwtService.generateToken(adminUser.getEmail());
        userToken = jwtService.generateToken(normalUser.getEmail());
    }

    @Test
    void shouldReturnProductsForAdmin() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath(
                        "$.content[0].name"
                ).value("Admin Product"))
                .andExpect(jsonPath(
                        "$.content[0].sku"
                ).value("ADMIN-PRODUCT-001"))
                .andExpect(jsonPath(
                        "$.content[0].status"
                ).value("ACTIVE"));
    }

    @Test
    void shouldReturnProductByIdForAdmin() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/products/{productId}", product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.id"
                ).value(product.getId()))
                .andExpect(jsonPath(
                        "$.name"
                ).value("Admin Product"))
                .andExpect(jsonPath(
                        "$.slug"
                ).value("admin-product"))
                .andExpect(jsonPath(
                        "$.sku"
                ).value("ADMIN-PRODUCT-001"))
                .andExpect(jsonPath(
                        "$.status"
                ).value("ACTIVE"));
    }

    @Test
    void shouldUpdateProductStatusForAdmin() throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/admin/products/{productId}/status",
                                product.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "OUT_OF_STOCK"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.id"
                ).value(product.getId()))
                .andExpect(jsonPath(
                        "$.status"
                ).value("OUT_OF_STOCK"));

        Product updatedProduct =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertEquals(
                ProductStatus.OUT_OF_STOCK,
                updatedProduct.getStatus()
        );
    }

    @Test
    void shouldForbidNormalUserFromAccessingAdminProducts() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/products")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/products/{productId}", 999999L)
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidStatus() throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/admin/products/{productId}/status",
                                product.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "INVALID_STATUS"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsMissing() throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/admin/products/{productId}/status",
                                product.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {}
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}