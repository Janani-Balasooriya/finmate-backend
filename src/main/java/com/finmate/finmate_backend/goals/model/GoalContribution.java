package com.finmate.finmate_backend.goals.model;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "goal_contributions")
@EntityListeners(AuditingEntityListener.class)
public class GoalContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // DB default is CURRENT_TIMESTAMP; we also fill it via JPA auditing
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    // FK: goal_contributions.users_id -> users.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // FK: goal_contributions.goals_id -> goals.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goals_id", nullable = false)
    private Goal goal;

    // FK: goal_contributions.accounts_id -> accounts.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;
}
