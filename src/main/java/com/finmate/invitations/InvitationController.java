package com.finmate.invitations;

import com.finmate.common.ApiResponse;
import com.finmate.invitations.dto.CreateInvitationRequest;
import com.finmate.invitations.dto.InvitationDto;
import com.finmate.users.User;
import com.finmate.users.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Invitation management endpoints")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all invitations", description = "Retrieve a list of all invitations")
    public ResponseEntity<ApiResponse<List<InvitationDto>>> getAllInvitations(Authentication authentication) {
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can view invitations");
        }
        
        List<Invitation> invitations = invitationService.getAllInvitations();
        List<InvitationDto> invitationDtos = invitations.stream()
                .map(InvitationDto::fromInvitation)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Invitations retrieved successfully", invitationDtos));
    }

    @PostMapping
    @Operation(summary = "Create invitation", description = "Create a new invitation for a user")
    public ResponseEntity<ApiResponse<InvitationDto>> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can create invitations");
        }
        
        Invitation invitation = invitationService.createInvitation(
                request.getEmail(), 
                currentUser
        );
        
        InvitationDto invitationDto = InvitationDto.fromInvitation(invitation);
        return ResponseEntity.ok(ApiResponse.success("Invitation created successfully", invitationDto));
    }

    @PostMapping("/{id}/resend")
    @Operation(summary = "Resend invitation", description = "Resend an existing invitation with a new token")
    public ResponseEntity<ApiResponse<InvitationDto>> resendInvitation(
            @Parameter(description = "Invitation ID") @PathVariable Long id,
            Authentication authentication) {
        
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can resend invitations");
        }
        
        Invitation invitation = invitationService.resendInvitation(id);
        InvitationDto invitationDto = InvitationDto.fromInvitation(invitation);
        return ResponseEntity.ok(ApiResponse.success("Invitation resent successfully", invitationDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete invitation", description = "Delete an invitation")
    public ResponseEntity<ApiResponse<Void>> deleteInvitation(
            @Parameter(description = "Invitation ID") @PathVariable Long id,
            Authentication authentication) {
        
        User currentUser = userService.getUserEntityByEmail(authentication.getName());
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can delete invitations");
        }
        
        invitationService.deleteInvitation(id);
        return ResponseEntity.ok(ApiResponse.success("Invitation deleted successfully", null));
    }
}
