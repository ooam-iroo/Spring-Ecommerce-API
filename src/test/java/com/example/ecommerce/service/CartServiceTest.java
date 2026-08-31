package com.example.ecommerce.service;

import com.example.ecommerce.dto.cart.CartItemAddRequest;
import com.example.ecommerce.dto.cart.CartItemResponse;
import com.example.ecommerce.dto.cart.CartItemUpdateRequest;
import com.example.ecommerce.dto.cart.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.CartServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String USER_EMAIL = "amir@example.com";
    private static final Long USER_ID = 1L;
    private static final Long CART_ID = 10L;
    private static final Long PRODUCT_ID = 100L;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUpSecurityContext() {

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        USER_EMAIL,
                        null,
                        "ROLE_USER"
                );

        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnExistingCart() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);

        when(user.getId()).thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(cart.getId())
                .thenReturn(CART_ID);

        when(cart.getItems())
                .thenReturn(List.of());

        CartResponse result = cartService.getCart();

        assertNotNull(result);
        assertEquals(CART_ID, result.cartId());
        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.total());

        verify(userRepository)
                .findByEmail(USER_EMAIL);

        verify(cartRepository)
                .findByUserId(USER_ID);

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    @Test
    void shouldCreateCartWhenCartDoesNotExist() {

        User user = mock(User.class);
        Cart savedCart = mock(Cart.class);

        when(user.getId()).thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty());

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(savedCart);

        when(savedCart.getId())
                .thenReturn(CART_ID);

        when(savedCart.getItems())
                .thenReturn(List.of());

        CartResponse result = cartService.getCart();

        assertNotNull(result);
        assertEquals(CART_ID, result.cartId());
        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.total());

        verify(userRepository)
                .findByEmail(USER_EMAIL);

        verify(userRepository)
                .findById(USER_ID);

        verify(cartRepository)
                .findByUserId(USER_ID);

        verify(cartRepository)
                .save(any(Cart.class));
    }

    @Test
    void shouldThrowExceptionWhenCurrentUserDoesNotExist() {

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCart()
        );

        verify(userRepository)
                .findByEmail(USER_EMAIL);

        verifyNoInteractions(cartRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticated() {

        SecurityContextHolder.clearContext();

        assertThrows(
                BusinessException.class,
                () -> cartService.getCart()
        );

        verifyNoInteractions(
                userRepository,
                cartRepository,
                productRepository
        );
    }

    @Test
    void shouldAddItemToCart() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);
        Product product = mock(Product.class);

        CartItemAddRequest request =
                new CartItemAddRequest(
                        PRODUCT_ID,
                        2
                );

        when(user.getId())
                .thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product));

        when(product.getStatus())
                .thenReturn(ProductStatus.ACTIVE);

        when(cart.getId())
                .thenReturn(CART_ID);

        when(cart.getItems())
                .thenReturn(List.of());

        CartResponse result =
                cartService.addItem(request);

        assertNotNull(result);
        assertEquals(CART_ID, result.cartId());
        assertTrue(result.items().isEmpty());
        assertEquals(
                BigDecimal.ZERO,
                result.total()
        );

        verify(productRepository)
                .findById(PRODUCT_ID);

        verify(cart)
                .addItem(product, 2);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.empty());

        CartItemAddRequest request =
                new CartItemAddRequest(
                        PRODUCT_ID,
                        2
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(request)
        );

        verify(productRepository)
                .findById(PRODUCT_ID);

        verify(cart, never())
                .addItem(any(Product.class), anyInt());
    }

    @Test
    void shouldRejectInactiveProduct() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);
        Product product = mock(Product.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product));

        when(product.getStatus())
                .thenReturn(ProductStatus.INACTIVE);

        when(product.getId())
                .thenReturn(PRODUCT_ID);

        CartItemAddRequest request =
                new CartItemAddRequest(
                        PRODUCT_ID,
                        2
                );

        assertThrows(
                BusinessException.class,
                () -> cartService.addItem(request)
        );

        verify(productRepository)
                .findById(PRODUCT_ID);

        verify(product)
                .getStatus();

        verify(product)
                .getId();

        verify(cart, never())
                .addItem(any(Product.class), anyInt());
    }

    @Test
    void shouldUpdateCartItemQuantity() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);

        CartItemUpdateRequest request =
                new CartItemUpdateRequest(5);

        when(user.getId())
                .thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(cart.getId())
                .thenReturn(CART_ID);

        when(cart.getItems())
                .thenReturn(List.of());

        CartResponse result =
                cartService.updateItem(
                        PRODUCT_ID,
                        request
                );

        assertNotNull(result);
        assertEquals(CART_ID, result.cartId());
        assertTrue(result.items().isEmpty());
        assertEquals(
                BigDecimal.ZERO,
                result.total()
        );

        verify(cart)
                .updateItemQuantity(
                        PRODUCT_ID,
                        5
                );
    }

    @Test
    void shouldRemoveItemFromCart() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        cartService.removeItem(PRODUCT_ID);

        verify(cart)
                .removeItem(PRODUCT_ID);
    }

    @Test
    void shouldClearCart() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        cartService.clearCart();

        verify(cart)
                .clear();
    }

    @Test
    void shouldCalculateCartTotal() {

        User user = mock(User.class);
        Cart cart = mock(Cart.class);

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        com.example.ecommerce.entity.CartItem item1 =
                mock(com.example.ecommerce.entity.CartItem.class);

        com.example.ecommerce.entity.CartItem item2 =
                mock(com.example.ecommerce.entity.CartItem.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(cart.getId())
                .thenReturn(CART_ID);

        when(cart.getItems())
                .thenReturn(List.of(item1, item2));

        when(item1.getProduct())
                .thenReturn(product1);

        when(item2.getProduct())
                .thenReturn(product2);

        when(product1.getId())
                .thenReturn(100L);

        when(product1.getName())
                .thenReturn("Keyboard");

        when(product1.getSku())
                .thenReturn("KB-001");

        when(product1.getPrice())
                .thenReturn(new BigDecimal("50.00"));

        when(item1.getQuantity())
                .thenReturn(2);

        when(product2.getId())
                .thenReturn(200L);

        when(product2.getName())
                .thenReturn("Mouse");

        when(product2.getSku())
                .thenReturn("MS-001");

        when(product2.getPrice())
                .thenReturn(new BigDecimal("25.00"));

        when(item2.getQuantity())
                .thenReturn(1);

        CartResponse result =
                cartService.getCart();

        assertNotNull(result);

        assertEquals(
                CART_ID,
                result.cartId()
        );

        assertEquals(
                2,
                result.items().size()
        );

        assertEquals(
                new BigDecimal("125.00"),
                result.total()
        );

        CartItemResponse firstItem =
                result.items().get(0);

        assertEquals(
                100L,
                firstItem.productId()
        );

        assertEquals(
                "Keyboard",
                firstItem.productName()
        );

        assertEquals(
                "KB-001",
                firstItem.sku()
        );

        assertEquals(
                new BigDecimal("50.00"),
                firstItem.price()
        );

        assertEquals(
                2,
                firstItem.quantity()
        );

        assertEquals(
                new BigDecimal("100.00"),
                firstItem.subtotal()
        );

        CartItemResponse secondItem =
                result.items().get(1);

        assertEquals(
                200L,
                secondItem.productId()
        );

        assertEquals(
                "Mouse",
                secondItem.productName()
        );

        assertEquals(
                "MS-001",
                secondItem.sku()
        );

        assertEquals(
                new BigDecimal("25.00"),
                secondItem.price()
        );

        assertEquals(
                1,
                secondItem.quantity()
        );

        assertEquals(
                new BigDecimal("25.00"),
                secondItem.subtotal()
        );
    }
}