package com.example.ecommerce.controller;

import com.example.ecommerce.dto.admin.product.AdminProductResponse;
import com.example.ecommerce.dto.admin.product.ProductStatusUpdateRequest;
import com.example.ecommerce.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<Page<AdminProductResponse>> findProducts(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminProductService.findProducts(pageable)
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductResponse> findProduct(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                adminProductService.findProduct(productId)
        );
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<AdminProductResponse> updateProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                adminProductService.updateProductStatus(
                        productId,
                        request
                )
        );
    }
}