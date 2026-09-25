package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.user.AdminUserResponse;
import com.example.ecommerce.dto.admin.user.UserRoleUpdateRequest;
import com.example.ecommerce.dto.admin.user.UserStatusUpdateRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User user;
    private User secondUser;

    @BeforeEach
    void setUp() {
        user = new User(
                "John",
                "Doe",
                "john@example.com",
                "password",
                "09120000000",
                UserStatus.ACTIVE,
                UserRole.USER
        );

        ReflectionTestUtils.setField(user, "id", 1L);

        secondUser = new User(
                "Jane",
                "Smith",
                "jane@example.com",
                "password",
                "09120000001",
                UserStatus.ACTIVE,
                UserRole.USER
        );

        ReflectionTestUtils.setField(secondUser, "id", 2L);
    }

    @Test
    void shouldFindAllUsers() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<User> userPage =
                new PageImpl<>(
                        List.of(user, secondUser),
                        pageable,
                        2
                );

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        Page<AdminUserResponse> result =
                adminUserService.findUsers(null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        assertEquals(
                "john@example.com",
                result.getContent().getFirst().email()
        );

        assertEquals(
                "jane@example.com",
                result.getContent().get(1).email()
        );

        verify(userRepository).findAll(pageable);
    }

    @Test
    void shouldFindUsersWhenSearchIsBlank() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<User> userPage =
                new PageImpl<>(
                        List.of(user),
                        pageable,
                        1
                );

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        Page<AdminUserResponse> result =
                adminUserService.findUsers("   ", pageable);

        assertEquals(1, result.getTotalElements());

        verify(userRepository).findAll(pageable);

        verify(
                userRepository,
                never()
        ).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                anyString(),
                anyString(),
                anyString(),
                any()
        );
    }

    @Test
    void shouldSearchUsers() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<User> userPage =
                new PageImpl<>(
                        List.of(user),
                        pageable,
                        1
                );

        when(
                userRepository
                        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                "john",
                                "john",
                                "john",
                                pageable
                        )
        ).thenReturn(userPage);

        Page<AdminUserResponse> result =
                adminUserService.findUsers("john", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
                "john@example.com",
                result.getContent().getFirst().email()
        );

        verify(
                userRepository
        ).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "john",
                "john",
                "john",
                pageable
        );
    }

    @Test
    void shouldFindUserById() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        AdminUserResponse response =
                adminUserService.findUser(user.getId());

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("john@example.com", response.email());
        assertEquals(UserStatus.ACTIVE, response.status());
        assertEquals(UserRole.USER, response.role());

        verify(userRepository).findById(user.getId());
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> adminUserService.findUser(999L)
                );

        assertEquals(
                "User not found: 999",
                exception.getMessage()
        );

        verify(userRepository).findById(999L);
    }

    @Test
    void shouldUpdateUserStatus() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(UserStatus.SUSPENDED);

        AdminUserResponse response =
                adminUserService.updateUserStatus(
                        user.getId(),
                        request
                );

        assertNotNull(response);
        assertEquals(
                UserStatus.SUSPENDED,
                response.status()
        );

        assertEquals(
                UserStatus.SUSPENDED,
                user.getStatus()
        );

        verify(userRepository).findById(user.getId());
    }

    @Test
    void shouldUpdateUserRole() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        UserRoleUpdateRequest request =
                new UserRoleUpdateRequest(UserRole.ADMIN);

        AdminUserResponse response =
                adminUserService.updateUserRole(
                        user.getId(),
                        request
                );

        assertNotNull(response);
        assertEquals(
                UserRole.ADMIN,
                response.role()
        );

        assertEquals(
                UserRole.ADMIN,
                user.getRole()
        );

        verify(userRepository).findById(user.getId());
    }

    @Test
    void shouldThrowWhenUpdatingStatusForNonExistingUser() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(UserStatus.SUSPENDED);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> adminUserService.updateUserStatus(
                                999L,
                                request
                        )
                );

        assertEquals(
                "User not found: 999",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenUpdatingRoleForNonExistingUser() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserRoleUpdateRequest request =
                new UserRoleUpdateRequest(UserRole.ADMIN);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> adminUserService.updateUserRole(
                                999L,
                                request
                        )
                );

        assertEquals(
                "User not found: 999",
                exception.getMessage()
        );
    }
}