package com.finmate.finmate_backend.auth.service;

import com.finmate.finmate_backend.auth.JwtUtil;
import com.finmate.finmate_backend.auth.dto.AuthResponse;
import com.finmate.finmate_backend.auth.dto.LoginRequest;
import com.finmate.finmate_backend.auth.dto.RegisterRequest;
import com.finmate.finmate_backend.families.model.FamilyInvite;
import com.finmate.finmate_backend.families.repository.FamilyInviteRepository;
import com.finmate.finmate_backend.users.model.User;
import com.finmate.finmate_backend.users.model.enums.Role;
import com.finmate.finmate_backend.users.model.enums.Status;
import com.finmate.finmate_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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
    private final FamilyInviteRepository familyInviteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Get user from database
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is active
            if (user.getStatus() != Status.ACTIVE) {
                throw new RuntimeException("Account is not active");
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            return new AuthResponse(token, user);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.MEMBER);
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Handle family invitation if provided
        if (registerRequest.getInviteToken() != null && !registerRequest.getInviteToken().isEmpty()) {
            Optional<FamilyInvite> invite = familyInviteRepository.findByToken(registerRequest.getInviteToken());
            if (invite.isPresent() && invite.get().getStatus() == com.finmate.finmate_backend.families.model.enums.InviteStatus.PENDING) {
                // Add user to the family
                user.setFamily(invite.get().getFamilies());
                
                // Update invite status to accepted
                invite.get().setStatus(com.finmate.finmate_backend.families.model.enums.InviteStatus.ACCEPTED);
                familyInviteRepository.save(invite.get());
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
