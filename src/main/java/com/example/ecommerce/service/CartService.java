package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;

import java.util.List;

public interface CartService {

    Cart findById(Long id);

    List<Cart> findAll();

    Cart save(Cart cart);

    void deleteById(Long id);
}