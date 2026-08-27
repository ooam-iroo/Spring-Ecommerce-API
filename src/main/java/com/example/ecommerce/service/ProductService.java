package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;

import java.util.List;

public interface ProductService {

    Product findById(Long id);

    List<Product> findAll();

    Product save(Product product);

    void deleteById(Long id);
}