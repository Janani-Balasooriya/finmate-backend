package com.finmate.finmate_backend.auth.controller;

import com.finmate.finmate_backend.auth.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@Tag(name = "Test", description = "Test endpoints for API validation")
public class TestController {

    @Operation(
        summary = "Public Test Endpoint",
        description = "A public endpoint that doesn't require authentication for testing purposes"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "Success",
                      "data": "This is a public endpoint"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<String>> publicEndpoint() {
        return ResponseEntity.ok(ApiResponse.success("This is a public endpoint"));
    }

    @Operation(
        summary = "Protected Test Endpoint",
        description = "A protected endpoint that requires JWT authentication for testing purposes"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "Success",
                      "data": "This is a protected endpoint. User: john@example.com"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token required"
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/protected")
    public ResponseEntity<ApiResponse<String>> protectedEndpoint(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success("This is a protected endpoint. User: " + username));
    }
}
