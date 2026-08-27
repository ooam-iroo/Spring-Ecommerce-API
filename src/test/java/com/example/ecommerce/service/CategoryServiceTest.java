package com.example.ecommerce.service;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.service.impl.CategoryServiceImpl;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void shouldReturnCategoryWhenCategoryExists() {

        Category category = mock(Category.class);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category result = categoryService.findById(1L);

        assertNotNull(result);
        assertSame(category, result);

        verify(categoryRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> categoryService.findById(1L)
        );

        verify(categoryRepository)
                .findById(1L);
    }

    @Test
    void shouldSaveCategory() {

        Category category = mock(Category.class);

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = categoryService.save(category);

        assertSame(category, result);

        verify(categoryRepository)
                .save(category);
    }

    @Test
    void shouldReturnAllCategories() {

        Category category1 = mock(Category.class);
        Category category2 = mock(Category.class);

        List<Category> categories = List.of(category1, category2);

        when(categoryRepository.findAll())
                .thenReturn(categories);

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
        assertSame(categories, result);

        verify(categoryRepository)
                .findAll();
    }
}