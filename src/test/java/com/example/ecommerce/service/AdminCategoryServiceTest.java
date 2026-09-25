package com.example.ecommerce.service;

import com.example.ecommerce.dto.admin.category.AdminCategoryResponse;
import com.example.ecommerce.dto.admin.category.CategoryRequest;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.service.impl.AdminCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AdminCategoryServiceImpl adminCategoryService;

    private Category parentCategory;
    private Category category;

    @BeforeEach
    void setUp() {
        parentCategory = new Category(
                "Electronics",
                "electronics",
                null
        );

        ReflectionTestUtils.setField(parentCategory, "id", 1L);

        category = new Category(
                "Laptops",
                "laptops",
                parentCategory
        );

        ReflectionTestUtils.setField(category, "id", 2L);
    }

    @Test
    void shouldFindAllCategories() {
        Pageable pageable = PageRequest.of(0, 20);

        PageImpl<Category> page = new PageImpl<>(
                List.of(parentCategory, category),
                pageable,
                2
        );

        when(categoryRepository.findAll(pageable))
                .thenReturn(page);

        var result = adminCategoryService.findCategories(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        AdminCategoryResponse first = result.getContent().get(0);

        assertEquals(1L, first.id());
        assertEquals("Electronics", first.name());
        assertEquals("electronics", first.slug());
        assertNull(first.parentId());
        assertNull(first.parentName());

        AdminCategoryResponse second = result.getContent().get(1);

        assertEquals(2L, second.id());
        assertEquals("Laptops", second.name());
        assertEquals("laptops", second.slug());
        assertEquals(1L, second.parentId());
        assertEquals("Electronics", second.parentName());

        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void shouldFindCategoryById() {
        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        AdminCategoryResponse result =
                adminCategoryService.findCategory(2L);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Laptops", result.name());
        assertEquals("laptops", result.slug());
        assertEquals(1L, result.parentId());
        assertEquals("Electronics", result.parentName());

        verify(categoryRepository).findById(2L);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminCategoryService.findCategory(999L)
        );

        verify(categoryRepository).findById(999L);
    }

    @Test
    void shouldCreateRootCategory() {
        CategoryRequest request = new CategoryRequest(
                "Books",
                "books",
                null
        );

        Category savedCategory = new Category(
                "Books",
                "books",
                null
        );

        ReflectionTestUtils.setField(savedCategory, "id", 3L);

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        AdminCategoryResponse result =
                adminCategoryService.createCategory(request);

        assertNotNull(result);
        assertEquals(3L, result.id());
        assertEquals("Books", result.name());
        assertEquals("books", result.slug());
        assertNull(result.parentId());
        assertNull(result.parentName());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldCreateChildCategory() {
        CategoryRequest request = new CategoryRequest(
                "Gaming Laptops",
                "gaming-laptops",
                1L
        );

        Category savedCategory = new Category(
                "Gaming Laptops",
                "gaming-laptops",
                parentCategory
        );

        ReflectionTestUtils.setField(savedCategory, "id", 3L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(parentCategory));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        AdminCategoryResponse result =
                adminCategoryService.createCategory(request);

        assertNotNull(result);
        assertEquals(3L, result.id());
        assertEquals("Gaming Laptops", result.name());
        assertEquals("gaming-laptops", result.slug());
        assertEquals(1L, result.parentId());
        assertEquals("Electronics", result.parentName());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenParentCategoryNotFound() {
        CategoryRequest request = new CategoryRequest(
                "Gaming Laptops",
                "gaming-laptops",
                999L
        );

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminCategoryService.createCategory(request)
        );

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldUpdateCategory() {
        CategoryRequest request = new CategoryRequest(
                "Gaming",
                "gaming",
                1L
        );

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(parentCategory));

        AdminCategoryResponse result =
                adminCategoryService.updateCategory(2L, request);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Gaming", result.name());
        assertEquals("gaming", result.slug());
        assertEquals(1L, result.parentId());
        assertEquals("Electronics", result.parentName());

        assertEquals("Gaming", category.getName());
        assertEquals("gaming", category.getSlug());
        assertSame(parentCategory, category.getParent());

        verify(categoryRepository).findById(2L);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldUpdateCategoryToRootCategory() {
        CategoryRequest request = new CategoryRequest(
                "Computers",
                "computers",
                null
        );

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        AdminCategoryResponse result =
                adminCategoryService.updateCategory(2L, request);

        assertNotNull(result);
        assertEquals("Computers", result.name());
        assertEquals("computers", result.slug());
        assertNull(result.parentId());
        assertNull(result.parentName());

        assertEquals("Computers", category.getName());
        assertEquals("computers", category.getSlug());
        assertNull(category.getParent());

        verify(categoryRepository).findById(2L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingCategory() {
        CategoryRequest request = new CategoryRequest(
                "Gaming",
                "gaming",
                null
        );

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminCategoryService.updateCategory(999L, request)
        );

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithNonExistingParent() {
        CategoryRequest request = new CategoryRequest(
                "Gaming",
                "gaming",
                999L
        );

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminCategoryService.updateCategory(2L, request)
        );

        verify(categoryRepository).findById(2L);
        verify(categoryRepository).findById(999L);
    }

    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        adminCategoryService.deleteCategory(2L);

        verify(categoryRepository).findById(2L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingCategory() {
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminCategoryService.deleteCategory(999L)
        );

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never())
                .delete(any(Category.class));
    }
}