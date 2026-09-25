package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.category.AdminCategoryResponse;
import com.example.ecommerce.dto.admin.category.CategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCategoryService {

    Page<AdminCategoryResponse> findCategories(Pageable pageable);

    AdminCategoryResponse findCategory(Long categoryId);

    AdminCategoryResponse createCategory(CategoryRequest request);

    AdminCategoryResponse updateCategory(Long categoryId, CategoryRequest request);

    void deleteCategory(Long categoryId);
}