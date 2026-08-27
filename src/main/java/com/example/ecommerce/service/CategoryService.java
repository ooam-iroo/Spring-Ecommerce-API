package com.example.ecommerce.service;

import com.example.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService {

    Category findById(Long id);

    List<Category> findAll();

    Category save(Category category);

    void deleteById(Long id);
}