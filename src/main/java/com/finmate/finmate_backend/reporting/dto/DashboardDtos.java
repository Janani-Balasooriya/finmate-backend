package com.finmate.finmate_backend.reporting.dto;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardDtos {

    public record DashboardStats(
            BigDecimal totalIncomeThisMonth,
            BigDecimal totalExpensesThisMonth,
            BigDecimal netSavings,
            BigDecimal savingsRate
    ) {}

    public record MonthlyTrendData(
            String month,
            BigDecimal income,
            BigDecimal expenses
    ) {}

    public record BudgetComparisonData(
            String category,
            java.math.BigDecimal budget,
            java.math.BigDecimal actual,
            String status
    ) {}

    public record BudgetComparison(
            java.util.List<BudgetComparisonData> data
    ) {}

    public record CashFlowData(
            String month,
            java.math.BigDecimal cashFlow,
            java.math.BigDecimal cumulative
    ) {}

    public record CashFlowAnalysis(
            java.util.List<CashFlowData> data
    ) {}

    public record IncomeExpenseTrend(
            java.util.List<MonthlyTrendData> data
    ) {}
}