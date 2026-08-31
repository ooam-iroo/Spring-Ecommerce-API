package com.example.ecommerce.dto.address;

public record AddressResponse(
        Long id,
        String title,
        String country,
        String city,
        String postalCode,
        String street,
        String building,
        String unit,
        boolean defaultAddress
) {
}