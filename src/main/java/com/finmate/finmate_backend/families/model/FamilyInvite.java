package com.finmate.finmate_backend.families.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
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

    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // FK: family_invites.families_id → families.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "families_id")
    private Family families;
}
