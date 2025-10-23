package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType; 
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;  // mock out service (no DB access)

    // Mock data
    private User createMockUser() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("alice@example.com");
        user.setPassword("password123");
        user.setSecurityQuestion("Favorite color?");
        user.setSecurityAnswer("Blue");
        return user;
    }

    @Test
    void testGetAllUsers() throws Exception {
        User user = createMockUser();
        when(service.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alice@example.com"));
    }

    @Test
    void testGetUserById_Found() throws Exception {
        User user = createMockUser();
        when(service.getUserById(1)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.securityAnswer").value("Blue"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(service.getUserById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRegisterUser() throws Exception {
        User user = createMockUser();
        when(service.createUser(user)).thenReturn(user);

        String json = """
                {
                    "email": "alice@example.com",
                    "password": "password123",
                    "securityQuestion": "Favorite color?",
                    "securityAnswer": "Blue"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}