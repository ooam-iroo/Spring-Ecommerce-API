package com.example.ecommerce.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(
            mappedBy = "parent",
            fetch = FetchType.LAZY
    )
    private List<Category> children = new ArrayList<>();

    public Category(String name, String slug, Category parent) {
        this.name = name;
        this.slug = slug;
        this.parent = parent;
    }

    public void update(
            String name,
            String slug,
            Category parent
    ) {
        this.name = name;
        this.slug = slug;
        this.parent = parent;
    }
}