package com.example.ecommerce.entity;

import com.example.ecommerce.entity.status.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
}