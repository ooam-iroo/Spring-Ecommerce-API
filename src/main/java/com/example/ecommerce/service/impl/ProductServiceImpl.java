package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.product.ProductCreateRequest;
import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.dto.product.ProductUpdateRequest;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ecommerce.dto.product.ProductFilterRequest;
import com.example.ecommerce.repository.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse create(ProductCreateRequest request) {

        if (productRepository.existsBySlug(request.slug())) {
            throw new BusinessException(
                    "Product slug already exists: " + request.slug()
            );
        }

        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException(
                    "Product SKU already exists: " + request.sku()
            );
        }

        Category category = findCategory(request.categoryId());

        Product product = new Product(
                category,
                request.name(),
                request.slug(),
                request.description(),
                request.price(),
                request.sku(),
                request.status()
        );

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(findProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(
            ProductFilterRequest filter,
            Pageable pageable
    ) {

        Specification<Product> specification =
                Specification
                        .where(ProductSpecification.nameContains(filter.name()))
                        .and(ProductSpecification.hasCategory(filter.categoryId()))
                        .and(ProductSpecification.hasStatus(filter.status()))
                        .and(ProductSpecification.priceGreaterThanOrEqual(filter.minPrice()))
                        .and(ProductSpecification.priceLessThanOrEqual(filter.maxPrice()));

        return productRepository
                .findAll(specification, pageable)
                .map(this::toResponse);
    }

    @Override
    public ProductResponse update(
            Long id,
            ProductUpdateRequest request
    ) {
        Product product = findProduct(id);

        if (!product.getSlug().equals(request.slug())
                && productRepository.existsBySlug(request.slug())) {
            throw new BusinessException(
                    "Product slug already exists: " + request.slug()
            );
        }

        if (!product.getSku().equals(request.sku())
                && productRepository.existsBySku(request.sku())) {
            throw new BusinessException(
                    "Product SKU already exists: " + request.sku()
            );
        }

        Category category = findCategory(request.categoryId());

        product.update(
                category,
                request.name(),
                request.slug(),
                request.description(),
                request.price(),
                request.sku(),
                request.status()
        );

        return toResponse(product);
    }

    @Override
    public void deleteById(Long id) {
        Product product = findProduct(id);

        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + id
                        )
                );
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + id
                        )
                );
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
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