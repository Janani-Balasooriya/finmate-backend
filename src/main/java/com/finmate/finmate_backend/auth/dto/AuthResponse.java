package com.finmate.finmate_backend.auth.dto;

import com.finmate.finmate_backend.users.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private User user;
    
    public AuthResponse(String accessToken, User user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}
