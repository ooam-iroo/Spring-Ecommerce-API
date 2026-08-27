package com.example.ecommerce.service;

import com.example.ecommerce.entity.User;

import java.util.List;

public interface UserService {

    User findById(Long id);

    List<User> findAll();

    User save(User user);

    void deleteById(Long id);
}