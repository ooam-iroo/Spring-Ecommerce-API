package com.example.ecommerce.service;

import com.example.ecommerce.dto.review.ReviewCreateRequest;
import com.example.ecommerce.dto.review.ReviewResponse;
import com.example.ecommerce.dto.review.ReviewUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(
            Long productId,
            ReviewCreateRequest request
    );

    Page<ReviewResponse> findProductReviews(
            Long productId,
            Pageable pageable
    );

    ReviewResponse updateReview(
            Long reviewId,
            ReviewUpdateRequest request
    );

    void deleteReview(Long reviewId);
}