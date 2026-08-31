package com.example.ecommerce.service;

import com.example.ecommerce.dto.address.AddressCreateRequest;
import com.example.ecommerce.dto.address.AddressResponse;
import com.example.ecommerce.dto.address.AddressUpdateRequest;

import java.util.List;

public interface AddressService {

    AddressResponse create(
            Long userId,
            AddressCreateRequest request
    );

    AddressResponse findById(
            Long userId,
            Long addressId
    );

    List<AddressResponse> findAll(
            Long userId
    );

    AddressResponse update(
            Long userId,
            Long addressId,
            AddressUpdateRequest request
    );

    void deleteById(
            Long userId,
            Long addressId
    );

    AddressResponse setDefault(
            Long userId,
            Long addressId
    );
}