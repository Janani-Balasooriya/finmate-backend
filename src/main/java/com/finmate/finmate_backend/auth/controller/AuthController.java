package com.finmate.finmate_backend.auth.controller;

import com.finmate.finmate_backend.auth.dto.ApiResponse;
import com.finmate.finmate_backend.auth.dto.AuthResponse;
import com.finmate.finmate_backend.auth.dto.LoginRequest;
import com.finmate.finmate_backend.auth.dto.RegisterRequest;
import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.users.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "User Login",
        description = "Authenticate user with email and password, returns JWT token and user information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "Login successful",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "tokenType": "Bearer",
                        "user": {
                          "id": 1,
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john@example.com",
                          "role": "MEMBER",
                          "status": "ACTIVE"
                        }
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "message": "Invalid email or password",
                      "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(
        summary = "User Registration",
        description = "Register a new user account. Optionally accepts family invitation token."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Registration successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "Registration successful",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "tokenType": "Bearer",
                        "user": {
                          "id": 1,
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john@example.com",
                          "role": "MEMBER",
                          "status": "ACTIVE"
                        }
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Registration failed - validation error or email already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "message": "Email already exists",
                      "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(
        summary = "Get Current User",
        description = "Get information about the currently authenticated user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User information retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "User retrieved successfully",
                      "data": {
                        "id": 1,
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john@example.com",
                        "role": "MEMBER",
                        "status": "ACTIVE",
                        "createdAt": "2024-01-01T00:00:00Z"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "message": "User not found",
                      "data": null
                    }
                    """
                )
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = authService.getCurrentUser(email);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(
        summary = "User Logout",
        description = "Logout the current user. In JWT-based authentication, logout is handled client-side by removing the token."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "Logout successful",
                      "data": "Token should be removed from client"
                    }
                    """
                )
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // In JWT-based authentication, logout is typically handled on the client side
        // by removing the token from storage. However, we can provide an endpoint
        // for future token blacklisting if needed.
        return ResponseEntity.ok(ApiResponse.success("Logout successful", "Token should be removed from client"));
    }
}
