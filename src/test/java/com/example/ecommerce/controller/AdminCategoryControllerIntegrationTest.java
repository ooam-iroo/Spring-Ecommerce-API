package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User user;

    private Category parentCategory;
    private Category category;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(
                new User(
                        "Admin",
                        "User",
                        "admin@example.com",
                        passwordEncoder.encode("password"),
                        "09120000001",
                        UserStatus.ACTIVE,
                        UserRole.ADMIN
                )
        );

        user = userRepository.save(
                new User(
                        "John",
                        "Doe",
                        "john@example.com",
                        passwordEncoder.encode("password"),
                        "09120000002",
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );

        parentCategory = categoryRepository.save(
                new Category(
                        "Electronics",
                        "electronics",
                        null
                )
        );

        category = categoryRepository.save(
                new Category(
                        "Laptops",
                        "laptops",
                        parentCategory
                )
        );

        adminToken = jwtService.generateToken(admin.getEmail());
        userToken = jwtService.generateToken(user.getEmail());
    }

    @Test
    void shouldAllowAdminToFindCategories() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.content[0].slug").exists());
    }

    @Test
    void shouldAllowAdminToFindCategory() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Laptops"))
                .andExpect(jsonPath("$.slug").value("laptops"))
                .andExpect(jsonPath("$.parentId")
                        .value(parentCategory.getId()))
                .andExpect(jsonPath("$.parentName")
                        .value("Electronics"));
    }

    @Test
    void shouldAllowAdminToCreateRootCategory() throws Exception {
        String requestBody = """
                {
                    "name": "Books",
                    "slug": "books"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/admin/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Books"))
                .andExpect(jsonPath("$.slug").value("books"))
                .andExpect(jsonPath("$.parentId").doesNotExist());
    }

    @Test
    void shouldAllowAdminToCreateChildCategory() throws Exception {
        String requestBody = """
                {
                    "name": "Gaming Laptops",
                    "slug": "gaming-laptops",
                    "parentId": %d
                }
                """.formatted(parentCategory.getId());

        mockMvc.perform(
                        post("/api/v1/admin/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Gaming Laptops"))
                .andExpect(jsonPath("$.slug")
                        .value("gaming-laptops"))
                .andExpect(jsonPath("$.parentId")
                        .value(parentCategory.getId()))
                .andExpect(jsonPath("$.parentName")
                        .value("Electronics"));
    }

    @Test
    void shouldAllowAdminToUpdateCategory() throws Exception {
        String requestBody = """
                {
                    "name": "Gaming",
                    "slug": "gaming",
                    "parentId": %d
                }
                """.formatted(parentCategory.getId());

        mockMvc.perform(
                        put("/api/v1/admin/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Gaming"))
                .andExpect(jsonPath("$.slug").value("gaming"))
                .andExpect(jsonPath("$.parentId")
                        .value(parentCategory.getId()));
    }

    @Test
    void shouldAllowAdminToDeleteCategory() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/admin/categories/" + category.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnForbiddenForNormalUser() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/categories")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundForNonExistingCategory() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/categories/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenParentCategoryDoesNotExist()
            throws Exception {

        String requestBody = """
                {
                    "name": "Gaming",
                    "slug": "gaming",
                    "parentId": 999999
                }
                """;

        mockMvc.perform(
                        post("/api/v1/admin/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
        String requestBody = """
                {
                    "slug": "gaming"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/admin/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSlugIsMissing() throws Exception {
        String requestBody = """
                {
                    "name": "Gaming"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/admin/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}