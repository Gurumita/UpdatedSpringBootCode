package com.example.demo.Service;

import com.example.demo.Enums.Role;
import com.example.demo.Models.User;
import com.example.demo.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User createUser(User user) throws MessagingException {
        String password = generateRandomPassword();
        user.setPassword(password);
        User createdUser = userRepository.save(user);
        sendWelcomeEmail(user.getEmail(), user.getUsername(), password);
        return createdUser;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private void sendWelcomeEmail(String email, String username, String password) throws MessagingException {
        String subject = "Welcome to Our Platform!";
        String body = "Hello " + username + ",\n\nYour account has been created successfully.\nYour password is: " + password + "\n\nBest regards,\nThe Team";
        emailService.sendEmail(email, subject, body);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(int userId, String newName, String newEmail) throws MessagingException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            boolean nameChanged = false;
            boolean emailChanged = false;

            if (newName != null && !newName.isEmpty() && !newName.equals(user.getUsername())) {
                user.setUsername(newName);
                nameChanged = true;
            }

            if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
                user.setEmail(newEmail);
                emailChanged = true;
            }
            User updatedUser = userRepository.save(user);
            if (nameChanged || emailChanged) {
                String subject = "Account Information Update Notification";
                String body = "Hello " + user.getUsername() + ",\n\nYour account information has been successfully updated.";

                if (nameChanged) {
                    body += "\n\nNew Name: " + user.getUsername();
                }
                if (emailChanged) {
                    body += "\n\nNew Email: " + user.getEmail();
                }

                emailService.sendEmail(user.getEmail(), subject, body);
            }

            return updatedUser;
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User assignAccessLevel(int userId, String newRole) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            boolean roleChanged = false;

            if (!newRole.equalsIgnoreCase(user.getRole().name())) {
                user.setRole(Role.valueOf(newRole.toUpperCase()));
                roleChanged = true;
            }

            User updatedUser = userRepository.save(user);

            if (roleChanged) {
                String subject = "Role Update Notification";
                String body = "Hello " + user.getUsername() + ",\n\nYour role has been updated.\n\n" +
                        "New Role: " + user.getRole().name();

                try {
                    emailService.sendEmail(user.getEmail(), subject, body);
                } catch (MessagingException e) {
                    throw new RuntimeException("Failed to send email: " + e.getMessage());
                }
            }

            return updatedUser;
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User findByNameAndEmail(String username, String email) {
        return userRepository.findByUsernameAndEmail(username, email);
    }


    public void updatePassword(int userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setPassword(newPassword);

        userRepository.save(user);
    }
}
