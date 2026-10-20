package com.example.ecommerce.cache;

import com.example.ecommerce.config.CacheNames;
import com.example.ecommerce.dto.product.ProductCreateRequest;
import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.dto.product.ProductUpdateRequest;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.status.ProductStatus;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductCacheIntegrationTest extends RedisIntegrationTestBase {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    private Category category;
    private String productSlug;
    private String productSku;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(CacheNames.PRODUCTS);

        if (cache != null) {
            cache.clear();
        }

        String uniqueSlug = "electronics-" + System.nanoTime();
        productSlug = "laptop-" + UUID.randomUUID();
        productSku = "SKU-" + UUID.randomUUID();

        category = categoryRepository.save(
                new Category(
                        "Electronics",
                        uniqueSlug,
                        null
                )
        );
    }

    @Test
    void shouldCacheProductAfterFirstRead() {
        Product product = productRepository.save(
                new Product(
                        category,
                        "Laptop",
                        productSlug,
                        "Test laptop",
                        new BigDecimal("1000.00"),
                        productSku,
                        ProductStatus.ACTIVE
                )
        );

        ProductResponse firstResult =
                productService.findById(product.getId());

        assertNotNull(firstResult);

        Cache cache = cacheManager.getCache(CacheNames.PRODUCTS);

        assertNotNull(cache);

        Cache.ValueWrapper cachedValue =
                cache.get("id:" + product.getId());

        assertNotNull(cachedValue);
        assertNotNull(cachedValue.get());
    }

    @Test
    void shouldEvictProductCacheAfterUpdate() {
        Product product = productRepository.save(
                new Product(
                        category,
                        "Laptop",
                        productSlug,
                        "Test laptop",
                        new BigDecimal("1000.00"),
                        productSku,
                        ProductStatus.ACTIVE
                )
        );

        productService.findById(product.getId());

        Cache cache = cacheManager.getCache(CacheNames.PRODUCTS);

        assertNotNull(cache);
        assertNotNull(cache.get("id:" + product.getId()));

        ProductUpdateRequest request = new ProductUpdateRequest(
                category.getId(),
                "Updated Laptop",
                "updated-" + productSlug,
                "Updated description",
                new BigDecimal("1200.00"),
                productSku,
                ProductStatus.ACTIVE
        );

        productService.update(product.getId(), request);

        assertNull(cache.get("id:" + product.getId()));
    }

    @Test
    void shouldEvictProductCacheAfterDelete() {
        Product product = productRepository.saveAndFlush(
                new Product(
                        category,
                        "Laptop",
                        productSlug,
                        "Test laptop",
                        new BigDecimal("1000.00"),
                        productSku,
                        ProductStatus.ACTIVE
                )
        );

        productService.findById(product.getId());

        Cache cache = cacheManager.getCache(CacheNames.PRODUCTS);

        assertNotNull(cache);
        assertNotNull(cache.get("id:" + product.getId()));

        productService.deleteById(product.getId());

        assertNull(cache.get("id:" + product.getId()));
    }
}
