package com.example.ecommerce.service;

import com.example.ecommerce.dto.address.AddressCreateRequest;
import com.example.ecommerce.dto.address.AddressResponse;
import com.example.ecommerce.dto.address.AddressUpdateRequest;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void shouldCreateAddress() {

        User user = mock(User.class);

        AddressCreateRequest request = new AddressCreateRequest(
                "Home",
                "Azerbaijan",
                "Baku",
                "AZ1000",
                "Nizami Street",
                "10",
                "5",
                false
        );

        Address address = mock(Address.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(addressRepository.save(any(Address.class)))
                .thenReturn(address);

        when(address.getId()).thenReturn(1L);
        when(address.getTitle()).thenReturn("Home");
        when(address.getCountry()).thenReturn("Azerbaijan");
        when(address.getCity()).thenReturn("Baku");
        when(address.getPostalCode()).thenReturn("AZ1000");
        when(address.getStreet()).thenReturn("Nizami Street");
        when(address.getBuilding()).thenReturn("10");
        when(address.getUnit()).thenReturn("5");
        when(address.isDefaultAddress()).thenReturn(false);

        AddressResponse result =
                addressService.create(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Home", result.title());
        assertEquals("Baku", result.city());
        assertFalse(result.defaultAddress());

        verify(userRepository)
                .findById(1L);

        verify(addressRepository)
                .save(any(Address.class));
    }

    @Test
    void shouldClearExistingDefaultAddressWhenCreatingDefaultAddress() {

        User user = mock(User.class);

        Address existingAddress = mock(Address.class);
        Address savedAddress = mock(Address.class);

        AddressCreateRequest request = new AddressCreateRequest(
                "Office",
                "Azerbaijan",
                "Baku",
                "AZ1000",
                "28 May Street",
                "20",
                "2",
                true
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserId(1L))
                .thenReturn(List.of(existingAddress));

        when(addressRepository.save(any(Address.class)))
                .thenReturn(savedAddress);

        when(savedAddress.getId()).thenReturn(2L);
        when(savedAddress.getTitle()).thenReturn("Office");
        when(savedAddress.getCountry()).thenReturn("Azerbaijan");
        when(savedAddress.getCity()).thenReturn("Baku");
        when(savedAddress.getPostalCode()).thenReturn("AZ1000");
        when(savedAddress.getStreet()).thenReturn("28 May Street");
        when(savedAddress.getBuilding()).thenReturn("20");
        when(savedAddress.getUnit()).thenReturn("2");
        when(savedAddress.isDefaultAddress()).thenReturn(true);

        AddressResponse result =
                addressService.create(1L, request);

        assertNotNull(result);
        assertTrue(result.defaultAddress());

        verify(existingAddress)
                .setDefaultAddress(false);

        verify(addressRepository)
                .save(any(Address.class));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        AddressCreateRequest request = new AddressCreateRequest(
                "Home",
                "Azerbaijan",
                "Baku",
                "AZ1000",
                "Nizami Street",
                "10",
                "5",
                false
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.create(1L, request)
        );

        verify(userRepository)
                .findById(1L);

        verify(addressRepository, never())
                .save(any(Address.class));
    }

    @Test
    void shouldReturnAddressWhenAddressExists() {

        Address address = mock(Address.class);

        when(addressRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(address));

        when(address.getId()).thenReturn(10L);
        when(address.getTitle()).thenReturn("Home");
        when(address.getCountry()).thenReturn("Azerbaijan");
        when(address.getCity()).thenReturn("Baku");
        when(address.getPostalCode()).thenReturn("AZ1000");
        when(address.getStreet()).thenReturn("Nizami Street");
        when(address.getBuilding()).thenReturn("10");
        when(address.getUnit()).thenReturn("5");
        when(address.isDefaultAddress()).thenReturn(false);

        AddressResponse result =
                addressService.findById(1L, 10L);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("Home", result.title());

        verify(addressRepository)
                .findByIdAndUserId(10L, 1L);
    }

    @Test
    void shouldThrowExceptionWhenAddressDoesNotExist() {

        when(addressRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.findById(1L, 10L)
        );

        verify(addressRepository)
                .findByIdAndUserId(10L, 1L);
    }

    @Test
    void shouldReturnAllAddresses() {

        User user = mock(User.class);

        Address address1 = mock(Address.class);
        Address address2 = mock(Address.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserId(1L))
                .thenReturn(List.of(address1, address2));

        when(address1.getId()).thenReturn(1L);
        when(address1.getTitle()).thenReturn("Home");

        when(address2.getId()).thenReturn(2L);
        when(address2.getTitle()).thenReturn("Office");

        List<AddressResponse> result =
                addressService.findAll(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(userRepository)
                .findById(1L);

        verify(addressRepository)
                .findByUserId(1L);
    }

    @Test
    void shouldUpdateAddress() {

        Address address = mock(Address.class);

        AddressUpdateRequest request = new AddressUpdateRequest(
                "Updated Home",
                "Azerbaijan",
                "Baku",
                "AZ1100",
                "Tbilisi Avenue",
                "15",
                "3",
                false
        );

        when(addressRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(address));

        AddressResponse result =
                addressService.update(1L, 10L, request);

        assertNotNull(result);

        verify(address)
                .update(
                        "Updated Home",
                        "Azerbaijan",
                        "Baku",
                        "AZ1100",
                        "Tbilisi Avenue",
                        "15",
                        "3",
                        false
                );
    }

    @Test
    void shouldDeleteAddress() {

        Address address = mock(Address.class);

        when(addressRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(address));

        addressService.deleteById(1L, 10L);

        verify(addressRepository)
                .findByIdAndUserId(10L, 1L);

        verify(addressRepository)
                .delete(address);
    }

    @Test
    void shouldSetAddressAsDefault() {

        Address address = mock(Address.class);
        Address existingAddress = mock(Address.class);

        when(addressRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(address));

        when(addressRepository.findByUserId(1L))
                .thenReturn(List.of(existingAddress, address));

        when(address.getId()).thenReturn(10L);
        when(address.getTitle()).thenReturn("Home");
        when(address.getCountry()).thenReturn("Azerbaijan");
        when(address.getCity()).thenReturn("Baku");
        when(address.getPostalCode()).thenReturn("AZ1000");
        when(address.getStreet()).thenReturn("Nizami Street");
        when(address.getBuilding()).thenReturn("10");
        when(address.getUnit()).thenReturn("5");
        when(address.isDefaultAddress()).thenReturn(true);

        AddressResponse result =
                addressService.setDefault(1L, 10L);

        assertNotNull(result);
        assertTrue(result.defaultAddress());

        verify(existingAddress)
                .setDefaultAddress(false);

        verify(address)
                .setDefaultAddress(true);
    }
}