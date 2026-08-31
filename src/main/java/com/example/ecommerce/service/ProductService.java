package com.example.ecommerce.service;

import com.example.ecommerce.dto.product.ProductCreateRequest;
import com.example.ecommerce.dto.product.ProductFilterRequest;
import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.dto.product.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(ProductCreateRequest request);

    ProductResponse findById(Long id);

    Page<ProductResponse> findAll(
            ProductFilterRequest filter,
            Pageable pageable
    );

    ProductResponse update(Long id, ProductUpdateRequest request);

    void deleteById(Long id);
}