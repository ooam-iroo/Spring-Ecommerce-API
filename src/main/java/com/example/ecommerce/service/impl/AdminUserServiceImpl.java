package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.admin.user.AdminUserResponse;
import com.example.ecommerce.dto.admin.user.UserRoleUpdateRequest;
import com.example.ecommerce.dto.admin.user.UserStatusUpdateRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> findUsers(
            String search,
            Pageable pageable
    ) {
        Page<User> users;

        if (search == null || search.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search,
                            search,
                            search,
                            pageable
                    );
        }

        return users.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse findUser(Long userId) {
        User user = findUserEntity(userId);
        return toResponse(user);
    }

    @Override
    public AdminUserResponse updateUserStatus(
            Long userId,
            UserStatusUpdateRequest request
    ) {
        User user = findUserEntity(userId);

        user.changeStatus(request.status());

        return toResponse(user);
    }

    @Override
    public AdminUserResponse updateUserRole(
            Long userId,
            UserRoleUpdateRequest request
    ) {
        User user = findUserEntity(userId);

        user.changeRole(request.role());

        return toResponse(user);
    }

    private User findUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}