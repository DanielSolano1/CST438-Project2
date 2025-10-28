package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
//import java.util.Map;
import java.util.Optional;


import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  @InjectMocks
  private UserController userController;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  // Helper to make dummy user
  private User createUser(Integer id, String email, String password) {
    User user = new User();
    user.setUserId(id);
    user.setEmail(email);
    user.setPassword(password);
    user.setSecurityQuestion("Favorite color?");
    user.setSecurityAnswer("Blue");
    return user;
  }

  // GET /api/users
  @Test
  void testGetAllUsers() throws Exception {
    User u1 = createUser(1, "a@example.com", "pass");
    User u2 = createUser(2, "b@example.com", "pass");

    when(userService.getAllUsers()).thenReturn(List.of(u1, u2));

    mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("a@example.com"))
            .andExpect(jsonPath("$[1].email").value("b@example.com"));
  }

  // GET /api/users/{id}
  @Test
  void testGetUserByIdFound() throws Exception {
    User u = createUser(1, "a@example.com", "pass");

    when(userService.getUserById(1)).thenReturn(Optional.of(u));

    mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("a@example.com"));
  }

  @Test
  void testGetUserByIdNotFound() throws Exception {
    when(userService.getUserById(1)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isNotFound());
  }

  // GET /api/users/email?email=...
  @Test
  void testGetUserByEmailFound() throws Exception {
    User u = createUser(1, "a@example.com", "pass");

    when(userService.getUserByEmail("a@example.com")).thenReturn(Optional.of(u));

    mockMvc.perform(get("/api/users/email?email=a@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("a@example.com"));
  }

  @Test
  void testGetUserByEmailNotFound() throws Exception {
    when(userService.getUserByEmail("x@example.com")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/users/email?email=x@example.com"))
            .andExpect(status().isNotFound());
  }

  // POST /api/users
  @Test
  void testRegisterUser() throws Exception {
    User newUser = createUser(10, "new@example.com", "1234");
    when(userService.createUser(any(User.class))).thenReturn(newUser);

    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "email":"new@example.com",
                            "password":"1234",
                            "securityQuestion":"Favorite color?",
                            "securityAnswer":"Blue"
                        }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("new@example.com"))
            .andExpect(jsonPath("$.securityQuestion").value("Favorite color?"))
            .andExpect(jsonPath("$.securityAnswer").value("Blue"));
  }

  @Test
  void testRegisterUserBadRequest() throws Exception {
    when(userService.createUser(any(User.class)))
            .thenThrow(new IllegalArgumentException("Email already exists"));

    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "email":"existing@example.com",
                            "password":"1234",
                            "securityQuestion":"Favorite color?",
                            "securityAnswer":"Blue"
                        }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Email already exists"));
  }

  // POST /api/users/login
  @Test
  void testLoginSuccess() throws Exception {
    User u = createUser(1, "a@example.com", "pass");
    when(userService.getUserByEmailAndPassword("a@example.com", "pass")).thenReturn(Optional.of(u));

    mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "email":"a@example.com",
                            "password":"pass"
                        }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("a@example.com"));
  }

  @Test
  void testLoginUnauthorized() throws Exception {
    when(userService.getUserByEmailAndPassword(anyString(), anyString())).thenReturn(Optional.empty());

    mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "email":"a@example.com",
                            "password":"wrong"
                        }
                    """))
            .andExpect(status().isUnauthorized());
  }

  // PUT /api/users/reset-password
  @Test
  void testResetPasswordSuccess() throws Exception {
    mockMvc.perform(put("/api/users/reset-password")
                    .param("email", "a@example.com")
                    .param("newPassword", "newpass"))
            .andExpect(status().isOk())
            .andExpect(content().string("Password updated successfully."));

    verify(userService).resetPassword("a@example.com", "newpass");
  }

  @Test
  void testResetPasswordBadRequest() throws Exception {
    doThrow(new IllegalArgumentException("User not found"))
        .when(userService).resetPassword(anyString(), anyString());

    mockMvc.perform(put("/api/users/reset-password")
                    .param("email", "missing@example.com")
                    .param("newPassword", "newpass"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("User not found"));
  }

  // DELETE /api/users/{id}
  @Test
  void testDeleteUser() throws Exception {
    mockMvc.perform(delete("/api/users/5"))
            .andExpect(status().isNoContent());

    verify(userService).deleteUser(5);
  }

  // GET /api/users/security-question?email=...
  @Test
  void testGetSecurityQuestionFound() throws Exception {
    User u = createUser(1, "a@example.com", "pass");

    when(userService.getUserByEmail("a@example.com")).thenReturn(Optional.of(u));

    mockMvc.perform(get("/api/users/security-question?email=a@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.securityQuestion").value("Favorite color?"));
  }

  @Test
  void testGetSecurityQuestionNotFound() throws Exception {
    when(userService.getUserByEmail("missing@example.com")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/users/security-question?email=missing@example.com"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("User not found"));
  }
}