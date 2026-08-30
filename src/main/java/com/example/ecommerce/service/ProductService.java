package com.example.ecommerce.service;

import com.example.ecommerce.dto.product.ProductCreateRequest;
import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.dto.product.ProductUpdateRequest;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductCreateRequest request);

    ProductResponse findById(Long id);

    List<ProductResponse> findAll();

    ProductResponse update(Long id, ProductUpdateRequest request);

    void deleteById(Long id);
}