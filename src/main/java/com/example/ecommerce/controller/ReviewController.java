package com.example.ecommerce.controller;

import com.example.ecommerce.dto.review.ReviewCreateRequest;
import com.example.ecommerce.dto.review.ReviewResponse;
import com.example.ecommerce.dto.review.ReviewUpdateRequest;
import com.example.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(productId, request));
    }

    @GetMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> findProductReviews(
            @PathVariable Long productId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                reviewService.findProductReviews(productId, pageable)
        );
    }

    @PutMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(
                reviewService.updateReview(reviewId, request)
        );
    }

    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}