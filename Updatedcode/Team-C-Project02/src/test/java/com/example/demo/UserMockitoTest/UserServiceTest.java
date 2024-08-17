package com.example.demo.UserMockitoTest;


import com.example.demo.Enums.Role;
import com.example.demo.Service.UserService;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.EmailService;
import com.example.demo.Models.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private User updatedUser;

    @BeforeEach
    public void setUp() {
        existingUser = new User();
        existingUser.setUsername("john");
        existingUser.setEmail("john@gmail.com");
        existingUser.setRole(Role.TEAM_MEMBER);

        updatedUser = new User();
        updatedUser.setUsername("JohnDoe");
        updatedUser.setEmail("john.doe@gmail.com");
        updatedUser.setRole(Role.ADMIN);

//        when(userRepository.findAll()).thenReturn(Arrays.asList(existingUser, updatedUser));
    }

    @Test
    public void testCreateUser() throws MessagingException {
        User user = new User();
        user.setUsername("abcd");
        user.setEmail("abcd@gmail.com");
        user.setPassword("password123");

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.createUser(user);

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    public void testUpdateUser_Success() throws MessagingException {
        int userId = 1;
        String newName = "abc";
        String newEmail = "abc@gmail.com";

        User updatedUser = new User();
        updatedUser.setUsername(newName);
        updatedUser.setEmail(newEmail);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, newName, newEmail);

        assertEquals(newName, result.getUsername());
        assertEquals(newEmail, result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmail(eq(newEmail), anyString(), anyString());
    }

    @Test
    public void testUpdateUser_Failure() throws MessagingException {
        int userId = 1;
        String newName = "abc";
        String newEmail = "abc@gmail.com";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(userId, newName, newEmail);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testGetAllUsers() {
        assertEquals(2, userService.getAllUsers().size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testAssignAccessLevel_RoleChanged() throws MessagingException {
        int userId = 1;
        String newRole = "admin";

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.assignAccessLevel(userId, newRole);

        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmail(eq(existingUser.getEmail()), anyString(), anyString());
    }

    @Test
    public void testAssignAccessLevel_RoleNotChanged() throws MessagingException {
        int userId = 1;
        String newRole = "team_member";
        User existingUser = new User();
        existingUser.setUserid(userId);
        existingUser.setRole(Role.TEAM_MEMBER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        User result = userService.assignAccessLevel(userId, newRole);
        assertNotNull(result, "The result should not be null");
        assertEquals(Role.TEAM_MEMBER, result.getRole(), "The role should be TEAM_MEMBER");
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }



    @Test
    public void testAssignAccessLevel_UserNotFound() throws MessagingException {
        int userId = 1;
        String newRole = "admin";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.assignAccessLevel(userId, newRole);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
