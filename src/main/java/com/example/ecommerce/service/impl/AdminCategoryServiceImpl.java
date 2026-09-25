package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.admin.category.AdminCategoryResponse;
import com.example.ecommerce.dto.admin.category.CategoryRequest;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCategoryResponse> findCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCategoryResponse findCategory(Long categoryId) {
        return toResponse(findCategoryEntity(categoryId));
    }

    @Override
    public AdminCategoryResponse createCategory(CategoryRequest request) {
        Category parent = findParent(request.parentId());

        Category category = new Category(
                request.name(),
                request.slug(),
                parent
        );

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Override
    public AdminCategoryResponse updateCategory(
            Long categoryId,
            CategoryRequest request
    ) {
        Category category = findCategoryEntity(categoryId);
        Category parent = findParent(request.parentId());

        category.update(
                request.name(),
                request.slug(),
                parent
        );

        return toResponse(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = findCategoryEntity(categoryId);

        categoryRepository.delete(category);
    }

    private Category findCategoryEntity(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + categoryId
                        )
                );
    }

    private Category findParent(Long parentId) {
        if (parentId == null) {
            return null;
        }

        return categoryRepository.findById(parentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parent category not found: " + parentId
                        )
                );
    }

    private AdminCategoryResponse toResponse(Category category) {
        Category parent = category.getParent();

        return new AdminCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                parent != null ? parent.getId() : null,
                parent != null ? parent.getName() : null,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}