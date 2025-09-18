package com.finmate.finmate_backend.expenses.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "category_items")
public class CategoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    // TINYINT(1) → boolean for soft delete
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // FK: category_items.expense_categories_id → expense_categories.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_categories_id", nullable = false)
    private ExpenseCategory expenseCategory;
}
