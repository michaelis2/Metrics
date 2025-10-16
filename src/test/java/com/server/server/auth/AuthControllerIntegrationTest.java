package com.server.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;

    @BeforeEach
    void setup() {
        userRepo.deleteAll();
        adminUser = new User();
        adminUser.setUsername("bruce");
        adminUser.setPassword(passwordEncoder.encode("password123")); // BCrypt
        adminUser.setEmail("bruce@egmail.com");
        adminUser.setActive(true);
        adminUser.setRole("ADMIN");
        adminUser.setCreatedDate(LocalDateTime.now());

        userRepo.save(adminUser);
    }

    @Test
    void testCreateUser_success() throws Exception {
        User newUser = new User();
        newUser.setUsername("alice");
        newUser.setPassword("alicepass");
        newUser.setEmail("alice@example.com");
        newUser.setRole("VIEWER");

        mockMvc.perform(post("/api/auth/users")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")) // mock admin JWT
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    void testGetAllUsers_success() throws Exception {
        mockMvc.perform(get("/api/auth/users")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("bruce"));
    }

    @Test
    void testGetUserById_success() throws Exception {
        mockMvc.perform(get("/api/auth/users/" + adminUser.getId())
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bruce"));
    }

    @Test
    void testLogin_success() throws Exception {
        AuthRequest loginRequest = new AuthRequest("bruce", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bruce"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testUpdateUserActiveStatus_success() throws Exception {
        mockMvc.perform(put("/api/auth/users/" + adminUser.getId() + "/active")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("false"))
                .andExpect(status().isOk())
                .andExpect(content().string("User active status updated"));
    }
}
