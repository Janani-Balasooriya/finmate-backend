package com.finmate.finmate_backend.families.service;

import com.finmate.finmate_backend.families.dto.MemberDto;
import com.finmate.finmate_backend.families.dto.UpdateFamilyCurrencyRequest;
import com.finmate.finmate_backend.families.model.Family;
import com.finmate.finmate_backend.families.repository.FamilyRepository;
import com.finmate.finmate_backend.users.model.User;
import com.finmate.finmate_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MemberDto> getFamilyMembers(Long familyId, String userEmail) {
        // Verify requesting user belongs to the family
        requireUserInFamily(familyId, userEmail);

        // Verify family exists
        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new EntityNotFoundException("Family not found"));

        // Get all users belonging to this family
        List<User> users = userRepository.findByFamiliesId(familyId);

        // Map to DTOs
        return users.stream()
            .map(user -> new MemberDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name().toLowerCase()
            ))
            .toList();
    }

    public Family updateFamilyCurrency(Long familyId, UpdateFamilyCurrencyRequest request, String userEmail) {
        // Verify requesting user belongs to the family
        requireUserInFamily(familyId, userEmail);

        // Verify family exists
        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new EntityNotFoundException("Family not found"));

        // Update currency
        family.setBaseCurrencyCode(request.baseCurrencyCode());

        // Save and return
        return familyRepository.save(family);
    }

    private void requireUserInFamily(Long familyId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getFamilies() == null || !user.getFamilies().getId().equals(familyId)) {
            throw new AccessDeniedException("Access denied to this family");
        }
    }
}
