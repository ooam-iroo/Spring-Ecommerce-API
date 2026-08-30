package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.status.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByNameContainingIgnoreCase(String name);
}