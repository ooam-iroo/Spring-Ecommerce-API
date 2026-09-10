package com.example.ecommerce.service;

import com.example.ecommerce.dto.review.ReviewCreateRequest;
import com.example.ecommerce.dto.review.ReviewResponse;
import com.example.ecommerce.dto.review.ReviewUpdateRequest;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Review;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.ReviewServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private User anotherUser;
    private Product product;
    private Review review;

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
        ReflectionTestUtils.setField(user, "id", 1L);

        anotherUser = new User(
                "Jane",
                "Doe",
                "jane@example.com",
                "password",
                "09120000001",
                UserStatus.ACTIVE,
                UserRole.USER
        );
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        Category category = new Category(
                "Electronics",
                "electronics",
                null
        );
        ReflectionTestUtils.setField(category, "id", 1L);
        ReflectionTestUtils.setField(category, "id", 1L);

        product = new Product(
                category,
                "iPhone 17",
                "iphone-17",
                "Latest iPhone",
                new BigDecimal("999.99"),
                "IPHONE-17",
                ProductStatus.ACTIVE
        );
        ReflectionTestUtils.setField(product, "id", 100L);

        review = new Review(
                user,
                product,
                (short) 5,
                "Excellent product!"
        );
        ReflectionTestUtils.setField(review, "id", 500L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateReviewForPurchasedProduct() {
        authenticate(user.getEmail());

        ReviewCreateRequest request = new ReviewCreateRequest(
                5,
                "Excellent product!"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(orderItemRepository.existsByOrderUserIdAndProductId(
                user.getId(),
                product.getId()
        )).thenReturn(true);

        when(reviewRepository.existsByUserIdAndProductId(
                user.getId(),
                product.getId()
        )).thenReturn(false);

        when(reviewRepository.save(any(Review.class)))
                .thenReturn(review);

        ReviewResponse response =
                reviewService.createReview(product.getId(), request);

        assertNotNull(response);
        assertEquals(review.getId(), response.id());
        assertEquals(product.getId(), response.productId());
        assertEquals(user.getId(), response.userId());
        assertEquals(5, response.rating());
        assertEquals("Excellent product!", response.comment());
        assertEquals("John Doe", response.userName());

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        authenticate(user.getEmail());

        ReviewCreateRequest request = new ReviewCreateRequest(
                5,
                "Great product"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.createReview(product.getId(), request)
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserHasNotPurchasedProduct() {
        authenticate(user.getEmail());

        ReviewCreateRequest request = new ReviewCreateRequest(
                5,
                "Great product"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(orderItemRepository.existsByOrderUserIdAndProductId(
                user.getId(),
                product.getId()
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.createReview(product.getId(), request)
        );

        assertEquals(
                "You can only review products you have purchased",
                exception.getMessage()
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserHasAlreadyReviewedProduct() {
        authenticate(user.getEmail());

        ReviewCreateRequest request = new ReviewCreateRequest(
                5,
                "Another review"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(orderItemRepository.existsByOrderUserIdAndProductId(
                user.getId(),
                product.getId()
        )).thenReturn(true);

        when(reviewRepository.existsByUserIdAndProductId(
                user.getId(),
                product.getId()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.createReview(product.getId(), request)
        );

        assertEquals(
                "User has already reviewed this product",
                exception.getMessage()
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldFindProductReviews() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<Review> reviewPage =
                new PageImpl<>(List.of(review), pageable, 1);

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(reviewRepository.findByProductId(
                product.getId(),
                pageable
        )).thenReturn(reviewPage);

        Page<ReviewResponse> result =
                reviewService.findProductReviews(product.getId(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(5, result.getContent().getFirst().rating());
        assertEquals(
                "Excellent product!",
                result.getContent().getFirst().comment()
        );

        verify(reviewRepository)
                .findByProductId(product.getId(), pageable);
    }

    @Test
    void shouldThrowWhenFindingReviewsForNonExistingProduct() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.findProductReviews(
                        product.getId(),
                        pageable
                )
        );

        verify(reviewRepository, never())
                .findByProductId(anyLong(), any());
    }

    @Test
    void shouldUpdateOwnReview() {
        authenticate(user.getEmail());

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                4,
                "Updated comment"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));

        ReviewResponse response =
                reviewService.updateReview(review.getId(), request);

        assertNotNull(response);
        assertEquals(4, response.rating());
        assertEquals("Updated comment", response.comment());

        assertEquals(Short.valueOf((short) 4), review.getRating());
        assertEquals("Updated comment", review.getComment());

        verify(reviewRepository).findById(review.getId());
    }

    @Test
    void shouldThrowWhenUpdatingAnotherUsersReview() {
        authenticate(anotherUser.getEmail());

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                4,
                "Updated comment"
        );

        when(userRepository.findByEmail(anotherUser.getEmail()))
                .thenReturn(Optional.of(anotherUser));

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.updateReview(
                        review.getId(),
                        request
                )
        );

        assertEquals(
                "You do not have access to this review",
                exception.getMessage()
        );

        assertEquals(Short.valueOf((short) 5), review.getRating());
        assertEquals("Excellent product!", review.getComment());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistingReview() {
        authenticate(user.getEmail());

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                4,
                "Updated comment"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.updateReview(
                        review.getId(),
                        request
                )
        );
    }

    @Test
    void shouldDeleteOwnReview() {
        authenticate(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));

        reviewService.deleteReview(review.getId());

        verify(reviewRepository).delete(review);
    }

    @Test
    void shouldThrowWhenDeletingAnotherUsersReview() {
        authenticate(anotherUser.getEmail());

        when(userRepository.findByEmail(anotherUser.getEmail()))
                .thenReturn(Optional.of(anotherUser));

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.deleteReview(review.getId())
        );

        assertEquals(
                "You do not have access to this review",
                exception.getMessage()
        );

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingNonExistingReview() {
        authenticate(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.deleteReview(review.getId())
        );

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenUserIsNotAuthenticated() {
        SecurityContextHolder.clearContext();

        ReviewCreateRequest request = new ReviewCreateRequest(
                5,
                "Great product"
        );

        assertThrows(
                BusinessException.class,
                () -> reviewService.createReview(
                        product.getId(),
                        request
                )
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExist() {
        authenticate(user.getEmail());

        ReviewCreateRequest request = new ReviewCreateRequest(
                5,
                "Great product"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.createReview(
                        product.getId(),
                        request
                )
        );

        verifyNoInteractions(productRepository);
        verifyNoInteractions(reviewRepository);
    }

    private void authenticate(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}