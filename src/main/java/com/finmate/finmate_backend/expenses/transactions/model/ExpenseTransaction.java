package com.finmate.finmate_backend.expenses.transactions.model;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import com.finmate.finmate_backend.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "expense_transactions")
public class ExpenseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    // FK: expense_transactions.category_items_id -> category_items.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_items_id", nullable = false)
    private CategoryItem categoryItem;

    // FK: expense_transactions.accounts_id -> accounts.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;

    // FK: expense_transactions.users_id -> users.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // FK: expense_transactions.subscriptions_id -> subscriptions.id (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriptions_id")
    private Subscription subscription;
}
