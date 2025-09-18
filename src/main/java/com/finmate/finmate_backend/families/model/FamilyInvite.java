package com.finmate.finmate_backend.families.model;

import com.finmate.finmate_backend.families.model.enums.InviteStatus;
import com.finmate.finmate_backend.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "family_invites")
@EntityListeners(AuditingEntityListener.class)
public class FamilyInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: family_invites.invited_by_user_id → users.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by_user_id", nullable = false)
    private User invitedByUser;

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "expires_at")
    private Instant expiresAt;

    // FK: family_invites.accepted_user_id → users.id (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_user_id")
    private User acceptedUser;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // FK: family_invites.families_id → families.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "families_id")
    private Family families;
}
