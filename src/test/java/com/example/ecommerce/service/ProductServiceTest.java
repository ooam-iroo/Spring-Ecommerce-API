package com.example.ecommerce.service;

import com.example.ecommerce.dto.product.ProductFilterRequest;
import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private com.example.ecommerce.repository.CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldReturnProductWhenProductExists() {

        Product product = mock(Product.class);
        Category category = mock(Category.class);

        when(product.getCategory())
                .thenReturn(category);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(1L);

        assertNotNull(result);

        verify(productRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.findById(1L)
        );

        verify(productRepository)
                .findById(1L);
    }

    @Test
    void shouldReturnPagedProducts() {

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        Category category1 = mock(Category.class);
        Category category2 = mock(Category.class);

        when(product1.getCategory())
                .thenReturn(category1);

        when(product2.getCategory())
                .thenReturn(category2);

        Page<Product> productPage =
                new PageImpl<>(List.of(product1, product2));

        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(productPage);

        ProductFilterRequest filter =
                new ProductFilterRequest(
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Page<ProductResponse> result =
                productService.findAll(filter, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());

        verify(productRepository)
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldApplyProductFilters() {

        Pageable pageable = PageRequest.of(0, 10);

        ProductFilterRequest filter =
                new ProductFilterRequest(
                        "iPhone",
                        1L,
                        ProductStatus.ACTIVE,
                        new BigDecimal("500"),
                        new BigDecimal("2000")
                );

        when(productRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(Page.empty(pageable));

        Page<ProductResponse> result =
                productService.findAll(filter, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository)
                .findAll(any(Specification.class), eq(pageable));
    }
}