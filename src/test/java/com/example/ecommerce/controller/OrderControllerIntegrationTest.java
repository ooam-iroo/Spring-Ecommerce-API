package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.CartRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingOrderWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/orders")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateOrderFromCart() throws Exception {

        User user = createUser("order-create@example.com");
        Product product = createProduct(
                "Laptop",
                "laptop-order-create",
                "LAPTOP-ORDER-001",
                "1500.00"
        );

        Cart cart = new Cart(user);
        cart.addItem(product, 2);
        cartRepository.save(cart);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.orderNumber").isString())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.subtotal").value(3000.00))
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.shippingCost").value(0))
                .andExpect(jsonPath("$.total").value(3000.00))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].sku").value("LAPTOP-ORDER-001"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(1500.00))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(3000.00));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingOrderFromEmptyCart()
            throws Exception {

        User user = createUser("empty-cart@example.com");

        Cart cart = new Cart(user);
        cartRepository.save(cart);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message")
                        .value("Cannot create order from an empty cart"));
    }

    @Test
    void shouldGetMyOrders() throws Exception {

        User user = createUser("my-orders@example.com");
        Product product = createProduct(
                "Keyboard",
                "keyboard-my-orders",
                "KEYBOARD-MY-001",
                "100.00"
        );

        Cart cart = new Cart(user);
        cart.addItem(product, 2);
        cartRepository.save(cart);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].total").value(200.00));
    }

    @Test
    void shouldGetOrderById() throws Exception {

        User user = createUser("find-order@example.com");
        Product product = createProduct(
                "Mouse",
                "mouse-find-order",
                "MOUSE-FIND-001",
                "50.00"
        );

        Cart cart = new Cart(user);
        cart.addItem(product, 3);
        cartRepository.save(cart);

        String token = jwtService.generateToken(user.getEmail());

        String response = mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = extractId(response);

        mockMvc.perform(
                        get("/api/v1/orders/" + orderId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.subtotal").value(150.00))
                .andExpect(jsonPath("$.total").value(150.00))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist()
            throws Exception {

        User user = createUser("missing-order@example.com");
        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        get("/api/v1/orders/999999")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Order not found: 999999"));
    }

    @Test
    void shouldPreventUserFromAccessingAnotherUsersOrder()
            throws Exception {

        User owner = createUser("order-owner@example.com");
        Product product = createProduct(
                "Monitor",
                "monitor-owner-order",
                "MONITOR-OWNER-001",
                "500.00"
        );

        Cart ownerCart = new Cart(owner);
        ownerCart.addItem(product, 1);
        cartRepository.save(ownerCart);

        String ownerToken = jwtService.generateToken(owner.getEmail());

        String response = mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + ownerToken)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = extractId(response);

        User anotherUser = createUser("another-user@example.com");
        String anotherUserToken =
                jwtService.generateToken(anotherUser.getEmail());

        mockMvc.perform(
                        get("/api/v1/orders/" + orderId)
                                .header(
                                        "Authorization",
                                        "Bearer " + anotherUserToken
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message")
                        .value("You do not have access to this order"));
    }

    @Test
    void shouldCancelPendingOrder() throws Exception {

        User user = createUser("cancel-order@example.com");
        Product product = createProduct(
                "Headphones",
                "headphones-cancel-order",
                "HEADPHONES-CANCEL-001",
                "200.00"
        );

        Cart cart = new Cart(user);
        cart.addItem(product, 1);
        cartRepository.save(cart);

        String token = jwtService.generateToken(user.getEmail());

        String response = mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = extractId(response);

        mockMvc.perform(
                        post("/api/v1/orders/" + orderId + "/cancel")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingOrdersWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/orders")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingOrderWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/orders/1")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenCancellingOrderWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/orders/1/cancel")
                )
                .andExpect(status().isForbidden());
    }

    private User createUser(String email) {

        User user = new User(
                "Test",
                "User",
                email,
                "password",
                null,
                UserStatus.ACTIVE,
                UserRole.USER
        );

        return userRepository.save(user);
    }

    private Product createProduct(
            String name,
            String slug,
            String sku,
            String price
    ) {

        Category category = new Category(
                "Electronics",
                "electronics-" + slug,
                null
        );

        categoryRepository.save(category);

        Product product = new Product(
                category,
                name,
                slug,
                "Test product",
                new BigDecimal(price),
                sku,
                ProductStatus.ACTIVE
        );

        return productRepository.save(product);
    }

    private long extractId(String response) {

        String marker = "\"id\":";
        int start = response.indexOf(marker) + marker.length();
        int end = response.indexOf(",", start);

        return Long.parseLong(
                response.substring(start, end).trim()
        );
    }
}