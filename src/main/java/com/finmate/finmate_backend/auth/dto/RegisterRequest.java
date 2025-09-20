package com.finmate.finmate_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request payload")
public class RegisterRequest {
    
    @Schema(description = "User's first name", example = "John", minLength = 2, maxLength = 120, required = true)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 120, message = "First name must be between 2 and 120 characters")
    private String firstName;
    
    @Schema(description = "User's last name", example = "Doe", minLength = 2, maxLength = 120, required = true)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 120, message = "Last name must be between 2 and 120 characters")
    private String lastName;
    
    @Schema(description = "User's email address", example = "john@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Schema(description = "User's password", example = "password123", minLength = 6, required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Schema(description = "Optional family invitation token", example = "abc123def456", required = false)
    private String inviteToken;
}
