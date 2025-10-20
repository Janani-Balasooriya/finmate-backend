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

    public record IncomeExpenseTrend(
            java.util.List<MonthlyTrendData> data
    ) {}
}