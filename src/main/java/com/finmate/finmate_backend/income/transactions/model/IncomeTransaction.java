package com.finmate.finmate_backend.income.transactions.model;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.income.sources.model.IncomeSource;
import com.finmate.finmate_backend.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "income_transactions")
public class IncomeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    // FK: income_transactions.accounts_id -> accounts.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;

    // FK: income_transactions.users_id -> users.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // FK: income_transactions.income_sources_id -> income_sources.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "income_sources_id", nullable = false)
    private IncomeSource incomeSource;
}
