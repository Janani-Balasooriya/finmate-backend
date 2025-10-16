package com.finmate.finmate_backend.families.controller;

import com.finmate.finmate_backend.auth.dto.ApiResponse;
import com.finmate.finmate_backend.families.dto.MemberDto;
import com.finmate.finmate_backend.families.service.FamilyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Family Members", description = "Family member management endpoints")
public class FamilyMemberController {

    private final FamilyService familyService;

    @GetMapping("/{familyId}/members")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<MemberDto>>> getFamilyMembers(
            @PathVariable Long familyId,
            Authentication authentication) {
        try {
            String email = authentication.getName();

            // TODO: Add authorization check to ensure user belongs to the family
            // For now, we'll let the service handle it

            List<MemberDto> members = familyService.getFamilyMembers(familyId);
            return ResponseEntity.ok(ApiResponse.success("Family members retrieved successfully", members));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}