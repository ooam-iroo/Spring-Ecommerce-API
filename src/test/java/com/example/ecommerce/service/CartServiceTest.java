package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.service.impl.CartServiceImpl;
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
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void shouldReturnCartWhenCartExists() {

        Cart cart = mock(Cart.class);

        when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        Cart result = cartService.findById(1L);

        assertNotNull(result);
        assertSame(cart, result);

        verify(cartRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCartDoesNotExist() {

        when(cartRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> cartService.findById(1L)
        );

        verify(cartRepository)
                .findById(1L);
    }

    @Test
    void shouldSaveCart() {

        Cart cart = mock(Cart.class);

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.save(cart);

        assertSame(cart, result);

        verify(cartRepository)
                .save(cart);
    }

    @Test
    void shouldReturnAllCarts() {

        Cart cart1 = mock(Cart.class);
        Cart cart2 = mock(Cart.class);

        List<Cart> carts = List.of(cart1, cart2);

        when(cartRepository.findAll())
                .thenReturn(carts);

        List<Cart> result = cartService.findAll();

        assertEquals(2, result.size());
        assertSame(carts, result);

        verify(cartRepository)
                .findAll();
    }
}