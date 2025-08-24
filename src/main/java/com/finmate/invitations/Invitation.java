package com.finmate.invitations;

import com.finmate.common.BaseEntity;
import com.finmate.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "invitations")
public class Invitation extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'MEMBER'")
    private InvitationRole role = InvitationRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'PENDING'")
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    public enum InvitationRole {
        ADMIN, MEMBER
    }

    public enum InvitationStatus {
        PENDING, ACCEPTED, DECLINED, EXPIRED
    }
}
