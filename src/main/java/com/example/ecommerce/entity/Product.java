package com.example.ecommerce.entity;

import com.example.ecommerce.entity.status.ProductStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @OneToOne(
            mappedBy = "product",
            fetch = FetchType.LAZY
    )
    private Inventory inventory;

    public Product(
            Category category,
            String name,
            String slug,
            String description,
            BigDecimal price,
            String sku,
            ProductStatus status
    ) {
        this.category = category;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.price = price;
        this.sku = sku;
        this.status = status;
    }

    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY
    )
    private List<Review> reviews = new ArrayList<>();
}