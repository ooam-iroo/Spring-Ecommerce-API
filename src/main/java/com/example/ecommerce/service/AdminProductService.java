package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.product.AdminProductResponse;
import com.example.ecommerce.dto.admin.product.ProductStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminProductService {

    Page<AdminProductResponse> findProducts(Pageable pageable);

    AdminProductResponse findProduct(Long productId);

    AdminProductResponse updateProductStatus(
            Long productId,
            ProductStatusUpdateRequest request
    );
}