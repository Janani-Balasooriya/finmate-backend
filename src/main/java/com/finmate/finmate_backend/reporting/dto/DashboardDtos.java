package com.finmate.finmate_backend.reporting.dto;

import java.math.BigDecimal;

public class DashboardDtos {

    public record DashboardStats(
            BigDecimal totalIncomeThisMonth,
            BigDecimal totalExpensesThisMonth,
            BigDecimal netSavings,
            BigDecimal savingsRate
    ) {}
}