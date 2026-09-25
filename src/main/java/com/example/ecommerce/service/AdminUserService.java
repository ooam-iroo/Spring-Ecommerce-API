package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.user.AdminUserResponse;
import com.example.ecommerce.dto.admin.user.UserRoleUpdateRequest;
import com.example.ecommerce.dto.admin.user.UserStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<AdminUserResponse> findUsers(
            String search,
            Pageable pageable
    );

    AdminUserResponse findUser(Long userId);

    AdminUserResponse updateUserStatus(
            Long userId,
            UserStatusUpdateRequest request
    );

    AdminUserResponse updateUserRole(
            Long userId,
            UserRoleUpdateRequest request
    );
}