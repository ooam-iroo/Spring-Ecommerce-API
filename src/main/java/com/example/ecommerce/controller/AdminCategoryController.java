package com.example.ecommerce.controller.admin;

import com.example.ecommerce.dto.admin.category.AdminCategoryResponse;
import com.example.ecommerce.dto.admin.category.CategoryRequest;
import com.example.ecommerce.service.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public ResponseEntity<Page<AdminCategoryResponse>> findCategories(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminCategoryService.findCategories(pageable)
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<AdminCategoryResponse> findCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                adminCategoryService.findCategory(categoryId)
        );
    }

    @PostMapping
    public ResponseEntity<AdminCategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(
                adminCategoryService.createCategory(request)
        );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<AdminCategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(
                adminCategoryService.updateCategory(categoryId, request)
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId
    ) {
        adminCategoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}