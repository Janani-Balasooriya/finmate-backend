package com.finmate.finmate_backend.insights.model;

import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.income.transactions.model.IncomeTransaction;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class FamilyContext {
    private Long familyId;
    private LocalDate now;

    // MTD totals
    private BigDecimal incomeMTD;
    private BigDecimal expenseMTD;

    // Recent transactions (last 60 days)
    private List<ExpenseTransaction> recentExpenses;
    private List<IncomeTransaction> recentIncomes;

    // Budgets with MTD spend
    private Map<Category, BigDecimal> budgetsWithMTDSpend;

    // Upcoming renewals (next 7 days)
    private List<Subscription> upcomingRenewals;

    // Checking balance (sum of active accounts)
    private BigDecimal checkingBalance;

    // Upcoming bills in 7 days
    private BigDecimal upcomingBills7d;

    // Month remaining budget (total budget - MTD spend)
    private BigDecimal monthRemainingBudget;

    // Underused budgets (budgets where spent < budget * 0.8)
    private BigDecimal underusedBudgets;
}