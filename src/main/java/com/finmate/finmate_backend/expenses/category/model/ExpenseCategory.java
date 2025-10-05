package com.finmate.finmate_backend.expenses.model;

import com.finmate.finmate_backend.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "expense_categories")
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "icon", length = 60)
    private String icon;

    @Column(name = "color", length = 16)
    private String color;

    @Column(name = "budget_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal budgetAmount = BigDecimal.ZERO;

    // TINYINT(1) → boolean
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // FK: expense_categories.families_id → families.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;
}
