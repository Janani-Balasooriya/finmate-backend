package com.finmate.finmate_backend.families.service;

import com.finmate.finmate_backend.families.model.Family;
import com.finmate.finmate_backend.families.model.FamilyInvite;
import com.finmate.finmate_backend.families.repository.FamilyInviteRepository;
import com.finmate.finmate_backend.families.repository.FamilyRepository;
import com.finmate.finmate_backend.users.model.User;
import com.finmate.finmate_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyInviteService {

    private final FamilyInviteRepository familyInviteRepository;
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;

    public FamilyInvite createInvite(Long familyId, String userEmail) {
        // Verify user exists and belongs to the family
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getFamilies().getId().equals(familyId)) {
            throw new RuntimeException("User does not belong to this family");
        }

        // Verify family exists
        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found"));

        // Generate token
        String token = UUID.randomUUID().toString();

        // Create invite (expires in 10 days)
        FamilyInvite invite = new FamilyInvite();
        invite.setToken(token);
        invite.setExpiresAt(Instant.now().plusSeconds(10 * 24 * 60 * 60)); // 10 days
        invite.setCreatedAt(Instant.now());
        invite.setFamilies(family);
        invite.setUser(user);

        return familyInviteRepository.save(invite);
    }

    public List<FamilyInvite> getFamilyInvites(Long familyId, String userEmail) {
        // Verify user exists and belongs to the family
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getFamilies().getId().equals(familyId)) {
            throw new RuntimeException("User does not belong to this family");
        }

        return familyInviteRepository.findAll().stream()
            .filter(invite -> invite.getFamilies().getId().equals(familyId))
            .toList();
    }

    public void revokeInvite(Long familyId, Long inviteId, String userEmail) {
        // Verify user exists and belongs to the family
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getFamilies().getId().equals(familyId)) {
            throw new RuntimeException("User does not belong to this family");
        }

        FamilyInvite invite = familyInviteRepository.findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getFamilies().getId().equals(familyId)) {
            throw new RuntimeException("Invite does not belong to this family");
        }

        familyInviteRepository.delete(invite);
    }

    @Transactional(readOnly = true)
    public FamilyInvite validateInviteToken(String token) {
        FamilyInvite invite = familyInviteRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid invite token"));

        // Check if expired
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Invite token has expired");
        }

        // Touch lazy-loaded properties to initialize them
        invite.getUser().getFirstName();
        invite.getFamilies().getId();

        return invite;
    }

    public void consumeInviteToken(String token) {
        FamilyInvite invite = familyInviteRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid invite token"));

        familyInviteRepository.delete(invite);
    }
}