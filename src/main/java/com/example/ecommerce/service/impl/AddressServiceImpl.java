package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.address.AddressCreateRequest;
import com.example.ecommerce.dto.address.AddressResponse;
import com.example.ecommerce.dto.address.AddressUpdateRequest;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressResponse create(
            Long userId,
            AddressCreateRequest request
    ) {

        User user = findUser(userId);

        if (request.defaultAddress()) {
            clearDefaultAddress(userId);
        }

        Address address = new Address(
                user,
                request.title(),
                request.country(),
                request.city(),
                request.postalCode(),
                request.street(),
                request.building(),
                request.unit(),
                request.defaultAddress()
        );

        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse findById(
            Long userId,
            Long addressId
    ) {
        return toResponse(findAddress(userId, addressId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> findAll(Long userId) {

        findUser(userId);

        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AddressResponse update(
            Long userId,
            Long addressId,
            AddressUpdateRequest request
    ) {

        Address address = findAddress(userId, addressId);

        if (request.defaultAddress()) {
            clearDefaultAddress(userId);
        }

        address.update(
                request.title(),
                request.country(),
                request.city(),
                request.postalCode(),
                request.street(),
                request.building(),
                request.unit(),
                request.defaultAddress()
        );

        return toResponse(address);
    }

    @Override
    public void deleteById(
            Long userId,
            Long addressId
    ) {

        Address address = findAddress(userId, addressId);

        addressRepository.delete(address);
    }

    @Override
    public AddressResponse setDefault(
            Long userId,
            Long addressId
    ) {

        Address address = findAddress(userId, addressId);

        clearDefaultAddress(userId);

        address.setDefaultAddress(true);

        return toResponse(address);
    }

    private User findUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );
    }

    private Address findAddress(
            Long userId,
            Long addressId
    ) {

        return addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Address not found: " + addressId
                        )
                );
    }

    private void clearDefaultAddress(Long userId) {

        addressRepository.findByUserId(userId)
                .forEach(address ->
                        address.setDefaultAddress(false)
                );
    }

    private AddressResponse toResponse(Address address) {

        return new AddressResponse(
                address.getId(),
                address.getTitle(),
                address.getCountry(),
                address.getCity(),
                address.getPostalCode(),
                address.getStreet(),
                address.getBuilding(),
                address.getUnit(),
                address.isDefaultAddress()
        );
    }
}