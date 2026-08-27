package com.example.ecommerce.service;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.UserServiceImpl;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldReturnUserWhenUserExists() {

        User user = mock(User.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertSame(user, result);

        verify(userRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> userService.findById(1L)
        );

        verify(userRepository)
                .findById(1L);
    }

    @Test
    void shouldSaveUser() {

        User user = mock(User.class);

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.save(user);

        assertSame(user, result);

        verify(userRepository)
                .save(user);
    }

    @Test
    void shouldReturnAllUsers() {

        User user1 = mock(User.class);
        User user2 = mock(User.class);

        List<User> users = List.of(user1, user2);

        when(userRepository.findAll())
                .thenReturn(users);

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        assertSame(users, result);

        verify(userRepository)
                .findAll();
    }
}