package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.review.ReviewCreateRequest;
import com.example.ecommerce.dto.review.ReviewResponse;
import com.example.ecommerce.dto.review.ReviewUpdateRequest;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Review;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public ReviewResponse createReview(
            Long productId,
            ReviewCreateRequest request
    ) {
        User currentUser = getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + productId
                        )
                );

        validateUserPurchasedProduct(
                currentUser.getId(),
                productId
        );

        if (reviewRepository.existsByUserIdAndProductId(
                currentUser.getId(),
                productId
        )) {
            throw new BusinessException(
                    "User has already reviewed this product"
            );
        }

        Review review = new Review(
                currentUser,
                product,
                request.rating().shortValue(),
                request.comment()
        );

        Review savedReview = reviewRepository.save(review);

        return toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> findProductReviews(
            Long productId,
            Pageable pageable
    ) {
        productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + productId
                        )
                );

        return reviewRepository
                .findByProductId(productId, pageable)
                .map(this::toResponse);
    }

    @Override
    public ReviewResponse updateReview(
            Long reviewId,
            ReviewUpdateRequest request
    ) {
        User currentUser = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found: " + reviewId
                        )
                );

        validateReviewOwnership(
                review,
                currentUser
        );

        review.update(
                request.rating().shortValue(),
                request.comment()
        );

        return toResponse(review);
    }

    @Override
    public void deleteReview(Long reviewId) {

        User currentUser = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found: " + reviewId
                        )
                );

        validateReviewOwnership(
                review,
                currentUser
        );

        reviewRepository.delete(review);
    }

    private void validateUserPurchasedProduct(
            Long userId,
            Long productId
    ) {
        boolean purchased =
                orderItemRepository.existsByOrderUserIdAndProductId(
                        userId,
                        productId
                );

        if (!purchased) {
            throw new BusinessException(
                    "You can only review products you have purchased"
            );
        }
    }

    private void validateReviewOwnership(
            Review review,
            User currentUser
    ) {
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(
                    "You do not have access to this review"
            );
        }
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new BusinessException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + email
                        )
                );
    }

    private ReviewResponse toResponse(Review review) {

        User user = review.getUser();
        Product product = review.getProduct();

        String userName =
                user.getFirstName() + " " + user.getLastName();

        return new ReviewResponse(
                review.getId(),
                product.getId(),
                user.getId(),
                userName,
                review.getRating().intValue(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}