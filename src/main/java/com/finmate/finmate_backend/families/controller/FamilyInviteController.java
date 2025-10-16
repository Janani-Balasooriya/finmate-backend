package com.finmate.finmate_backend.families.controller;

import com.finmate.finmate_backend.auth.dto.ApiResponse;
import com.finmate.finmate_backend.families.dto.InvitePreviewDto;
import com.finmate.finmate_backend.families.model.FamilyInvite;
import com.finmate.finmate_backend.families.service.FamilyInviteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Family Invites", description = "Family invitation management endpoints")
public class FamilyInviteController {

    private final FamilyInviteService familyInviteService;

    @PostMapping("/{familyId}/invites")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<FamilyInvite>> createInvite(
            @PathVariable Long familyId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            FamilyInvite invite = familyInviteService.createInvite(familyId, email);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invite link generated successfully", invite));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{familyId}/invites")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<FamilyInvite>>> getFamilyInvites(
            @PathVariable Long familyId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            List<FamilyInvite> invites = familyInviteService.getFamilyInvites(familyId, email);
            return ResponseEntity.ok(ApiResponse.success("Invites retrieved successfully", invites));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{familyId}/invites/{inviteId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> revokeInvite(
            @PathVariable Long familyId,
            @PathVariable Long inviteId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            familyInviteService.revokeInvite(familyId, inviteId, email);
            return ResponseEntity.ok(ApiResponse.success("Invite revoked successfully", "Invite has been revoked"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<ApiResponse<InvitePreviewDto>> validateInviteToken(@PathVariable String token) {
        try {
            FamilyInvite invite = familyInviteService.validateInviteToken(token);
            InvitePreviewDto dto = new InvitePreviewDto(
                invite.getUser().getFirstName(),
                invite.getFamilies().getId()
            );
            return ResponseEntity.ok(ApiResponse.success("Invite token is valid", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}