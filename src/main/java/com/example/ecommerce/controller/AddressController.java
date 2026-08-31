package com.example.ecommerce.controller;

import com.example.ecommerce.dto.address.AddressCreateRequest;
import com.example.ecommerce.dto.address.AddressResponse;
import com.example.ecommerce.dto.address.AddressUpdateRequest;
import com.example.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<AddressResponse> create(
            @PathVariable Long userId,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addressService.create(userId, request));
    }

    @GetMapping
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<List<AddressResponse>> findAll(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                addressService.findAll(userId)
        );
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<AddressResponse> findById(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        return ResponseEntity.ok(
                addressService.findById(userId, addressId)
        );
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<AddressResponse> update(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        return ResponseEntity.ok(
                addressService.update(
                        userId,
                        addressId,
                        request
                )
        );
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        addressService.deleteById(userId, addressId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<AddressResponse> setDefault(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        return ResponseEntity.ok(
                addressService.setDefault(
                        userId,
                        addressId
                )
        );
    }
}