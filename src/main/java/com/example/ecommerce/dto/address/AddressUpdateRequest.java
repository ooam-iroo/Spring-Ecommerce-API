package com.example.ecommerce.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(

        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        @Size(max = 100)
        String country,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 30)
        String postalCode,

        @NotBlank
        @Size(max = 255)
        String street,

        @Size(max = 50)
        String building,

        @Size(max = 50)
        String unit,

        boolean defaultAddress
) {
}