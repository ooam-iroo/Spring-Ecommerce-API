package com.example.ecommerce.service;

import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

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
    void shouldReturnAllProducts() {

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        Category category1 = mock(Category.class);
        Category category2 = mock(Category.class);

        when(product1.getCategory())
                .thenReturn(category1);

        when(product2.getCategory())
                .thenReturn(category2);

        when(productRepository.findAll())
                .thenReturn(List.of(product1, product2));

        List<ProductResponse> result = productService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(productRepository)
                .findAll();
    }
}