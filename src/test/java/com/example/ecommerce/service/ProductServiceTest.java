package com.example.ecommerce.service;

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

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertNotNull(result);
        assertSame(product, result);

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
    void shouldSaveProduct() {

        Product product = mock(Product.class);

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = productService.save(product);

        assertSame(product, result);

        verify(productRepository)
                .save(product);
    }

    @Test
    void shouldReturnAllProducts() {

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        List<Product> products = List.of(product1, product2);

        when(productRepository.findAll())
                .thenReturn(products);

        List<Product> result = productService.findAll();

        assertEquals(2, result.size());
        assertSame(products, result);

        verify(productRepository)
                .findAll();
    }
}