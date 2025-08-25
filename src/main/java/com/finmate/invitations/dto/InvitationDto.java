package com.finmate.invitations.dto;

import com.finmate.invitations.Invitation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationDto {
    private Long id;
    private String email;
    private String role;
    private String status;
    private String token;
    private LocalDateTime invitedAt;
    private LocalDateTime expiresAt;
    private String invitedByName;
    private String invitedByEmail;

    public static InvitationDto fromInvitation(Invitation invitation) {
        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setEmail(invitation.getEmail());
        dto.setRole(invitation.getRole().name());
        dto.setStatus(invitation.getStatus().name());
        dto.setToken(invitation.getToken());
        dto.setInvitedAt(invitation.getInvitedAt());
        dto.setExpiresAt(invitation.getExpiresAt());
        
        // Safely handle the invitedBy relationship
        if (invitation.getInvitedBy() != null) {
            dto.setInvitedByName(invitation.getInvitedBy().getName());
            dto.setInvitedByEmail(invitation.getInvitedBy().getEmail());
        }
        
        return dto;
    }
}
