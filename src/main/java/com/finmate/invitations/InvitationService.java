package com.finmate.invitations;

import com.finmate.users.User;
import com.finmate.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserService userService;

    public List<Invitation> getAllInvitations() {
        return invitationRepository.findAll();
    }

    public Invitation createInvitation(String email, User invitedBy) {
        // Check if invitation already exists for this email
        if (invitationRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Invitation already exists for this email");
        }

        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setRole(Invitation.InvitationRole.MEMBER); // All new members get MEMBER role
        invitation.setStatus(Invitation.InvitationStatus.PENDING);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setInvitedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days expiry
        invitation.setInvitedBy(invitedBy);

        return invitationRepository.save(invitation);
    }

    public Invitation resendInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Can only resend pending invitations");
        }

        // Generate new token and extend expiry
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation.setInvitedAt(LocalDateTime.now());

        return invitationRepository.save(invitation);
    }

    public void deleteInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        invitationRepository.delete(invitation);
    }

    public Invitation getInvitationById(Long id) {
        return invitationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
    }

    public Invitation getInvitationByToken(String token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));
    }
}
