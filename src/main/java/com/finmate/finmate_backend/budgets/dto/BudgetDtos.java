package com.finmate.finmate_backend.budgets.dto;

import java.math.BigDecimal;
import java.util.List;

public class BudgetDtos {

    public record BudgetItem(
            Long id,
            String name,
            String icon,
            String color,
            BigDecimal budgetAmount,
            BigDecimal spentAmount,
            BigDecimal remainingAmount,
            int percentUsed
    ) {}

    public record BudgetSummary(
            BigDecimal totalBudget,
            BigDecimal totalSpent,
            BigDecimal totalRemaining,
            int percentUsed
    ) {}

    public record BudgetResponse(
            BudgetSummary summary,
            List<BudgetItem> items
    ) {}

    public record CreateOrUpdateBudgetRequest(
            String name,
            String icon,
            String color,
            BigDecimal budgetAmount
    ) {}
}

