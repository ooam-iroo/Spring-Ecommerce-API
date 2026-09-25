package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    private User user;
    private User anotherUser;
    private Product product;

    private String userToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        user = createUser(
                "John",
                "Doe",
                "john@example.com"
        );

        anotherUser = createUser(
                "Jane",
                "Doe",
                "jane@example.com"
        );

        Category category = new Category(
                "Electronics",
                "electronics",
                null
        );

        category = categoryRepository.save(category);

        product = new Product(
                category,
                "iPhone 17",
                "iphone-17",
                "Latest iPhone",
                new BigDecimal("999.99"),
                "IPHONE-17",
                ProductStatus.ACTIVE
        );

        product = productRepository.save(product);

        userToken = jwtService.generateToken(user.getEmail());
        anotherUserToken = jwtService.generateToken(anotherUser.getEmail());
    }

    @Test
    void shouldCreateReviewForPurchasedProduct() throws Exception {
        mockPurchasedProduct(user);

        String request = """
                {
                    "rating": 5,
                    "comment": "Excellent product!"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/reviews", product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath(
                                "$.productId",
                                is(product.getId().intValue())
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.userId",
                                is(user.getId().intValue())
                        )
                )
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(
                        jsonPath(
                                "$.comment",
                                is("Excellent product!")
                        )
                );

        assertEquals(1, reviewRepository.count());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        String request = """
                {
                    "rating": 5,
                    "comment": "Excellent product!"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/reviews", product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectReviewWhenUserHasNotPurchasedProduct() throws Exception {
        when(orderItemRepository.existsByOrderUserIdAndProductId(
                user.getId(),
                product.getId()
        )).thenReturn(false);

        String request = """
                {
                    "rating": 5,
                    "comment": "Excellent product!"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/reviews", product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("You can only review products you have purchased")
                        )
                );

        assertEquals(0, reviewRepository.count());
    }

    @Test
    void shouldRejectDuplicateReview() throws Exception {
        mockPurchasedProduct(user);

        String firstRequest = """
                {
                    "rating": 5,
                    "comment": "Excellent product!"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/reviews", product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        String secondRequest = """
                {
                    "rating": 4,
                    "comment": "Updated opinion"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/reviews", product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("User has already reviewed this product")
                        )
                );

        assertEquals(1, reviewRepository.count());
    }

    @Test
    void shouldRejectInvalidRating() throws Exception {
        mockPurchasedProduct(user);

        String request = """
                {
                    "rating": 6,
                    "comment": "Invalid rating"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/reviews", product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        assertEquals(0, reviewRepository.count());
    }

    @Test
    void shouldRejectMissingRating() throws Exception {
        mockPurchasedProduct(user);

        String request = """
                {
                    "comment": "Rating is missing"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/reviews", product.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        assertEquals(0, reviewRepository.count());
    }

    @Test
    void shouldFindProductReviews() throws Exception {
        mockPurchasedProduct(user);

        createReview();

        mockMvc.perform(
                        get("/api/v1/products/{productId}/reviews", product.getId())
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rating", is(5)))
                .andExpect(
                        jsonPath(
                                "$.content[0].comment",
                                is("Excellent product!")
                        )
                )
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(
                        get(
                                "/api/v1/products/{productId}/reviews",
                                999999L
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateOwnReview() throws Exception {
        mockPurchasedProduct(user);

        Long reviewId = createReview();

        String request = """
                {
                    "rating": 4,
                    "comment": "Updated comment"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/reviews/{reviewId}", reviewId)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(4)))
                .andExpect(
                        jsonPath(
                                "$.comment",
                                is("Updated comment")
                        )
                );
    }

    @Test
    void shouldRejectUpdateByAnotherUser() throws Exception {
        mockPurchasedProduct(user);

        Long reviewId = createReview();

        String request = """
                {
                    "rating": 1,
                    "comment": "Hacked review"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/reviews/{reviewId}", reviewId)
                                .header(
                                        "Authorization",
                                        "Bearer " + anotherUserToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("You do not have access to this review")
                        )
                );
    }

    @Test
    void shouldDeleteOwnReview() throws Exception {
        mockPurchasedProduct(user);

        Long reviewId = createReview();

        mockMvc.perform(
                        delete("/api/v1/reviews/{reviewId}", reviewId)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andExpect(status().isNoContent());

        assertEquals(0, reviewRepository.count());
    }

    @Test
    void shouldRejectDeleteByAnotherUser() throws Exception {
        mockPurchasedProduct(user);

        Long reviewId = createReview();

        mockMvc.perform(
                        delete("/api/v1/reviews/{reviewId}", reviewId)
                                .header(
                                        "Authorization",
                                        "Bearer " + anotherUserToken
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("You do not have access to this review")
                        )
                );

        assertEquals(1, reviewRepository.count());
    }

    @Test
    void shouldReturnNotFoundWhenReviewDoesNotExist() throws Exception {
        mockMvc.perform(
                        put("/api/v1/reviews/{reviewId}", 999999L)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "rating": 5,
                                            "comment": "Test"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound());
    }

    private User createUser(
            String firstName,
            String lastName,
            String email
    ) {
        User user = new User(
                firstName,
                lastName,
                email,
                passwordEncoder.encode("Password123!"),
                "09120000000",
                UserStatus.ACTIVE,
                UserRole.USER
        );

        return userRepository.save(user);
    }

    private void mockPurchasedProduct(User user) {
        when(orderItemRepository.existsByOrderUserIdAndProductId(
                user.getId(),
                product.getId()
        )).thenReturn(true);
    }

    private Long createReview() throws Exception {
        mockPurchasedProduct(user);

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", product.getId())
                        .with(csrf())
                        .with(user(user.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5,
                                    "comment": "Excellent product!"
                                }
                                """))
                .andExpect(status().isCreated());

        return reviewRepository.findByUserIdAndProductId(
                user.getId(),
                product.getId()
        ).orElseThrow().getId();
    }
}