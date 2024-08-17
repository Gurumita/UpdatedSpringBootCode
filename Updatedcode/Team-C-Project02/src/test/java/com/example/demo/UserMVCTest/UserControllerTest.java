package com.example.demo.UserMVCTest;

import com.example.demo.Controller.UserController;
import com.example.demo.Enums.Role;
import com.example.demo.Models.User;
import com.example.demo.Service.EmailService;
import com.example.demo.Service.OtpService;
import com.example.demo.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private OtpService otpService;


    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("abcd");

        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"abcd\" }"))
                .andExpect(status().isCreated())
                .andExpect(content().string("User created successfully: abcd"));
    }

    @Test
    void testCreateUserFailure() throws Exception {
        when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Creation error"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"abcd\" }"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error creating user: Creation error"));
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        int userId = 1;
        String newName = "abcd";
        String newEmail = "abcd@gmail.com";
        User mockUser = new User();
        mockUser.setUsername(newName);
        mockUser.setEmail(newEmail);

        when(userService.updateUser(userId, newName, newEmail)).thenReturn(mockUser);
        mockMvc.perform(put("/api/users/update/{userId}", userId)
                        .param("newName", newName)
                        .param("newEmail", newEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully: " + newName));
    }

    @Test
    public void testUpdateUser_Failure() throws Exception {
        int userId = 1;
        String newName = "abcd";
        String newEmail = "abcd@gmail.com";

        when(userService.updateUser(userId, newName, newEmail))
                .thenThrow(new RuntimeException("User not found"));
        mockMvc.perform(put("/api/users/update/{userId}", userId)
                        .param("newName", newName)
                        .param("newEmail", newEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error updating user: User not found"));
    }

    @Test
    void testAssignAccessLevelSuccess() throws Exception {
        int userId = 1;
        String newRole = "ADMIN";

        User updatedUser = new User();
        updatedUser.setUserid(userId);
        updatedUser.setRole(Role.valueOf(newRole));

        Mockito.when(userService.assignAccessLevel(userId, newRole)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/assign-role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("newRole", newRole))))
                .andExpect(status().isOk())
                .andExpect(content().string("Role updated successfully to: ADMIN"));
    }

    @Test
    void testAssignAccessLevelFailure() throws Exception {
        int userId = 1;
        String newRole = "ADMIN";
        String errorMessage = "User not found";

        Mockito.when(userService.assignAccessLevel(userId, newRole)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(put("/api/users/assign-role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("newRole", newRole))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error updating role: " + errorMessage));
    }
}
