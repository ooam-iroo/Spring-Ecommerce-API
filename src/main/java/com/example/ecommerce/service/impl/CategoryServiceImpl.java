package com.example.ecommerce.service.impl;

import com.example.ecommerce.config.CacheNames;
import com.example.ecommerce.dto.category.CategoryCreateRequest;
import com.example.ecommerce.dto.category.CategoryResponse;
import com.example.ecommerce.dto.category.CategoryUpdateRequest;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.CATEGORIES,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = CacheNames.PRODUCTS,
                    allEntries = true
            )
    })
    public CategoryResponse create(CategoryCreateRequest request) {

        if (categoryRepository.existsBySlug(request.slug())) {
            throw new BusinessException(
                    "Category slug already exists: " + request.slug()
            );
        }

        Category parent = findParent(request.parentId());

        Category category = new Category(
                request.name(),
                request.slug(),
                parent
        );

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.CATEGORIES,
            key = "'id:' + #id"
    )
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(findCategory(id));
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.CATEGORIES,
            key = "'list'"
    )
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.CATEGORIES,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = CacheNames.PRODUCTS,
                    allEntries = true
            )
    })
    public CategoryResponse update(
            Long id,
            CategoryUpdateRequest request
    ) {
        Category category = findCategory(id);

        if (!category.getSlug().equals(request.slug())
                && categoryRepository.existsBySlug(request.slug())) {
            throw new BusinessException(
                    "Category slug already exists: " + request.slug()
            );
        }

        Category parent = findParent(request.parentId());

        if (parent != null && parent.getId().equals(id)) {
            throw new BusinessException(
                    "A category cannot be its own parent"
            );
        }

        category.update(
                request.name(),
                request.slug(),
                parent
        );

        return toResponse(category);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.CATEGORIES,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = CacheNames.PRODUCTS,
                    allEntries = true
            )
    })
    public void deleteById(Long id) {
        Category category = findCategory(id);

        if (!category.getChildren().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete category with child categories"
            );
        }

        categoryRepository.delete(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + id
                        )
                );
    }

    private Category findParent(Long parentId) {
        if (parentId == null) {
            return null;
        }

        return findCategory(parentId);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getParent() != null
                        ? category.getParent().getId()
                        : null
        );
    }
}