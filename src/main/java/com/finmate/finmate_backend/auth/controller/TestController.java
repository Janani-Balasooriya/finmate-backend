package com.finmate.finmate_backend.auth.controller;

import com.finmate.finmate_backend.auth.dto.ApiResponse;
import com.finmate.finmate_backend.users.model.User;
import com.finmate.finmate_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/public")
    public ApiResponse<String> publicEndpoint() {
        return ApiResponse.success("Success", "This is a public endpoint");
    }

    @GetMapping("/protected")
    public ApiResponse<String> protectedEndpoint() {
        return ApiResponse.success("Success", "This is a protected endpoint");
    }

    @PostMapping("/create-test-user")
    public ApiResponse<User> createTestUser() {
        try {
            // Check if user already exists
            if (userRepository.existsByEmail("test@example.com")) {
                User existingUser = userRepository.findByEmail("test@example.com").orElse(null);
                return ApiResponse.success("User already exists", existingUser);
            }

            // Create test user
            User user = new User();
            user.setFirstName("Test");
            user.setLastName("User");
            user.setEmail("test@example.com");
            user.setPasswordHash(passwordEncoder.encode("password123"));
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            // Role and Status will use their default values from the User model

            User savedUser = userRepository.save(user);
            return ApiResponse.success("Test user created", savedUser);
        } catch (Exception e) {
            return ApiResponse.error("Failed to create test user: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ApiResponse.success("Users retrieved", users);
        } catch (Exception e) {
            return ApiResponse.error("Failed to retrieve users: " + e.getMessage());
        }
    }

    @PostMapping("/test-password")
    public ApiResponse<String> testPassword(@RequestParam String email, @RequestParam String password) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ApiResponse.error("User not found");
            }

            boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
            return ApiResponse.success("Password test result", 
                "Password matches: " + matches + ", Stored hash: " + user.getPasswordHash());
        } catch (Exception e) {
            return ApiResponse.error("Password test failed: " + e.getMessage());
        }
    }
}