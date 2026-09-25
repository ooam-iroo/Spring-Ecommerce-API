package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.product.AdminProductResponse;
import com.example.ecommerce.dto.admin.product.ProductStatusUpdateRequest;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.impl.AdminProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AdminProductServiceImpl adminProductService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category(
                "Electronics",
                "electronics",
                null
        );

        ReflectionTestUtils.setField(
                category,
                "id",
                10L
        );

        product = new Product(
                category,
                "iPhone 15",
                "iphone-15",
                "Apple iPhone 15",
                new BigDecimal("999.99"),
                "IPHONE-15-001",
                ProductStatus.ACTIVE
        );

        ReflectionTestUtils.setField(
                product,
                "id",
                1L
        );
    }

    @Test
    void shouldFindAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        Page<AdminProductResponse> result =
                adminProductService.findProducts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        AdminProductResponse response = result.getContent().get(0);

        assertEquals(1L, response.id());
        assertEquals(10L, response.categoryId());
        assertEquals("Electronics", response.categoryName());
        assertEquals("iPhone 15", response.name());
        assertEquals("iphone-15", response.slug());
        assertEquals(
                new BigDecimal("999.99"),
                response.price()
        );
        assertEquals("IPHONE-15-001", response.sku());
        assertEquals(ProductStatus.ACTIVE, response.status());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void shouldFindProductById() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        AdminProductResponse result =
                adminProductService.findProduct(1L);

        assertEquals(1L, result.id());
        assertEquals(10L, result.categoryId());
        assertEquals("Electronics", result.categoryName());
        assertEquals("iPhone 15", result.name());
        assertEquals("iphone-15", result.slug());
        assertEquals(
                new BigDecimal("999.99"),
                result.price()
        );
        assertEquals("IPHONE-15-001", result.sku());
        assertEquals(ProductStatus.ACTIVE, result.status());

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminProductService.findProduct(999L)
        );

        verify(productRepository).findById(999L);
    }

    @Test
    void shouldUpdateProductStatus() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductStatusUpdateRequest request =
                new ProductStatusUpdateRequest(
                        ProductStatus.OUT_OF_STOCK
                );

        AdminProductResponse result =
                adminProductService.updateProductStatus(
                        1L,
                        request
                );

        assertEquals(
                ProductStatus.OUT_OF_STOCK,
                result.status()
        );

        assertEquals(
                ProductStatus.OUT_OF_STOCK,
                product.getStatus()
        );

        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldUpdateProductStatusToInactive() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductStatusUpdateRequest request =
                new ProductStatusUpdateRequest(
                        ProductStatus.INACTIVE
                );

        AdminProductResponse result =
                adminProductService.updateProductStatus(
                        1L,
                        request
                );

        assertEquals(
                ProductStatus.INACTIVE,
                result.status()
        );

        assertEquals(
                ProductStatus.INACTIVE,
                product.getStatus()
        );

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistingProduct() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ProductStatusUpdateRequest request =
                new ProductStatusUpdateRequest(
                        ProductStatus.OUT_OF_STOCK
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminProductService.updateProductStatus(
                        999L,
                        request
                )
        );

        verify(productRepository).findById(999L);
        verifyNoMoreInteractions(productRepository);
    }
}