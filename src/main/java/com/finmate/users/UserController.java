package com.finmate.users;

import com.finmate.common.ApiResponse;
import com.finmate.users.dto.UserDto;
import com.finmate.users.dto.RoleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all members", description = "Retrieve a list of all members")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllMembers(Authentication authentication) {
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can view members");
        }
        
        List<UserDto> members = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID", description = "Retrieve a specific member by ID")
    public ResponseEntity<ApiResponse<UserDto>> getMemberById(
            @Parameter(description = "Member ID") @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can view member details");
        }
        
        UserDto member = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Member retrieved successfully", member));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Update member role", description = "Update a member's role")
    public ResponseEntity<ApiResponse<UserDto>> updateMemberRole(
            @Parameter(description = "Member ID") @PathVariable Long id,
            @RequestBody RoleUpdateRequest request,
            Authentication authentication) {
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can update member roles");
        }
        
        // Prevent changing own role
        if (currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Cannot change your own role");
        }
        
        UserDto updatedMember = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", updatedMember));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove member", description = "Remove a member from the system")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(description = "Member ID") @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can remove members");
        }
        
        // Prevent removing yourself
        if (currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Cannot remove yourself");
        }
        
        userService.removeUser(id);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
