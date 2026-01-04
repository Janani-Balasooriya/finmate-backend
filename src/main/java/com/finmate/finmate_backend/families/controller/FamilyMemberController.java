package com.finmate.finmate_backend.families.controller;

import com.finmate.finmate_backend.auth.dto.ApiResponse;
import com.finmate.finmate_backend.families.dto.MemberDto;
import com.finmate.finmate_backend.families.dto.UpdateFamilyCurrencyRequest;
import com.finmate.finmate_backend.families.service.FamilyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
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

            List<MemberDto> members = familyService.getFamilyMembers(familyId, email);
            return ResponseEntity.ok(ApiResponse.success("Family members retrieved successfully", members));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You are not allowed to access this family"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{familyId}/currency")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> updateFamilyCurrency(
            @PathVariable Long familyId,
            @RequestBody UpdateFamilyCurrencyRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();

            familyService.updateFamilyCurrency(familyId, request, email);
            return ResponseEntity.ok(ApiResponse.success("Family currency updated successfully", null));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You are not allowed to update this family"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
