package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.admin.product.AdminProductResponse;
import com.example.ecommerce.dto.admin.product.ProductStatusUpdateRequest;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProductResponse> findProducts(Pageable pageable) {
        return productRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductResponse findProduct(Long productId) {
        return toResponse(findProductEntity(productId));
    }

    @Override
    public AdminProductResponse updateProductStatus(
            Long productId,
            ProductStatusUpdateRequest request
    ) {
        Product product = findProductEntity(productId);

        product.changeStatus(request.status());

        return toResponse(product);
    }

    private Product findProductEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + productId
                        )
                );
    }

    private AdminProductResponse toResponse(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getStatus()
        );
    }
}