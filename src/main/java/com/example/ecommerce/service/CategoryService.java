package com.example.ecommerce.service;

import com.example.ecommerce.dto.category.CategoryCreateRequest;
import com.example.ecommerce.dto.category.CategoryResponse;
import com.example.ecommerce.dto.category.CategoryUpdateRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryCreateRequest request);

    CategoryResponse findById(Long id);

    List<CategoryResponse> findAll();

    CategoryResponse update(Long id, CategoryUpdateRequest request);

    void deleteById(Long id);
}