package com.example.ecommerce.controller;

import com.example.ecommerce.dto.admin.user.AdminUserResponse;
import com.example.ecommerce.dto.admin.user.UserRoleUpdateRequest;
import com.example.ecommerce.dto.admin.user.UserStatusUpdateRequest;
import com.example.ecommerce.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> findUsers(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminUserService.findUsers(search, pageable)
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> findUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                adminUserService.findUser(userId)
        );
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                adminUserService.updateUserStatus(userId, request)
        );
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return ResponseEntity.ok(
                adminUserService.updateUserRole(userId, request)
        );
    }
}