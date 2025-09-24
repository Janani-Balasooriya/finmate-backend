package com.finmate.finmate_backend.income.model;

import com.finmate.finmate_backend.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "income_sources")
public class IncomeSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "icon", length = 60)
    private String icon;

    @Column(name = "color", length = 16)
    private String color;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // FK:
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;
}
