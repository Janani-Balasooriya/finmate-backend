package com.finmate.users.dto;

import com.finmate.users.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    
    @NotNull(message = "Role is required")
    private User.UserRole role;
}
