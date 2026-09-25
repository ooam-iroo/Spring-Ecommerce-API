package com.example.ecommerce.controller;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.UserRole;
import com.example.ecommerce.entity.status.UserStatus;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private User normalUser;
    private User targetUser;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.save(
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

        normalUser = userRepository.save(
                new User(
                        "Normal",
                        "User",
                        "user@example.com",
                        passwordEncoder.encode("password"),
                        "09120000002",
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );

        targetUser = userRepository.save(
                new User(
                        "Target",
                        "User",
                        "target@example.com",
                        passwordEncoder.encode("password"),
                        "09120000003",
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );

        adminToken = jwtService.generateToken(adminUser.getEmail());
        userToken = jwtService.generateToken(normalUser.getEmail());
    }

    @Test
    void shouldReturnUsersForAdmin() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].email").value(
                        org.hamcrest.Matchers.hasItem("admin@example.com")
                ));
    }

    @Test
    void shouldSearchUsersForAdmin() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/users")
                                .param("search", "target@example.com")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("target@example.com"));
    }

    @Test
    void shouldReturnUserByIdForAdmin() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/users/{userId}", targetUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUser.getId()))
                .andExpect(jsonPath("$.email").value("target@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldUpdateUserStatusForAdmin() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/admin/users/{userId}/status", targetUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "status": "SUSPENDED"
                                    }
                                    """)
                )
                .andDo(result -> System.out.println(
                        "STATUS UPDATE RESPONSE: " +
                                result.getResponse().getContentAsString()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUser.getId()))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void shouldUpdateUserRoleForAdmin() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/admin/users/{userId}/role", targetUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "role": "ADMIN"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUser.getId()))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                UserRole.ADMIN,
                updatedUser.getRole()
        );
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/users/{userId}", 999999L)
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/users")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldForbidNormalUserFromAccessingAdminEndpoints() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestForInvalidStatus() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/admin/users/{userId}/status", targetUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "INVALID_STATUS"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidRole() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/admin/users/{userId}/role", targetUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "role": "INVALID_ROLE"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsMissing() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/admin/users/{userId}/status", targetUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {}
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRoleIsMissing() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/admin/users/{userId}/role", targetUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {}
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}