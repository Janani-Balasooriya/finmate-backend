package com.finmate.finmate_backend.auth.service;

import com.finmate.finmate_backend.auth.JwtUtil;
import com.finmate.finmate_backend.auth.dto.AuthResponse;
import com.finmate.finmate_backend.auth.dto.LoginRequest;
import com.finmate.finmate_backend.auth.dto.RegisterRequest;
import com.finmate.finmate_backend.families.model.Family;
import com.finmate.finmate_backend.families.model.FamilyInvite;
import com.finmate.finmate_backend.families.repository.FamilyInviteRepository;
import com.finmate.finmate_backend.families.repository.FamilyRepository;
import com.finmate.finmate_backend.users.model.User;
import com.finmate.finmate_backend.users.model.enums.Role;
import com.finmate.finmate_backend.users.model.enums.Status;
import com.finmate.finmate_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyInviteRepository familyInviteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Get user from database
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password - no user in database"));

            // Check if user is active
            if (user.getStatus() != Status.active) {
                throw new RuntimeException("Account is not active");
            }

            // Verify password manually
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Invalid email or password - password doesn't match");
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            return new AuthResponse(token, user);

        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password - catch exception");
        }
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new family for the user
        Family family = new Family();
        family.setCreatedAt(Instant.now());
        Family savedFamily = familyRepository.save(family);

        // Create new user
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.member);
        user.setStatus(Status.active);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setFamilies(savedFamily);

        // Handle family invitation if provided
        if (registerRequest.getInviteToken() != null && !registerRequest.getInviteToken().isEmpty()) {
            Optional<FamilyInvite> invite = familyInviteRepository.findByToken(registerRequest.getInviteToken());
            if (invite.isPresent()) {
                // Add user to the family
                user.setFamilies(invite.get().getFamilies());

                // Update invite status to accepted (assuming there's a status field, but it's not in the model)
                // invite.get().setStatus(com.finmate.finmate_backend.families.model.enums.InviteStatus.ACCEPTED);
                // familyInviteRepository.save(invite.get());
            }
        }

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(token, savedUser);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
