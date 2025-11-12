package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FreeToSpendRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        List<InsightDto> insights = new ArrayList<>();
        BigDecimal balance = context.getCheckingBalance();
        BigDecimal upcomingBills = context.getUpcomingBills7d();
        BigDecimal remainingBudget = context.getMonthRemainingBudget();
        BigDecimal underused = context.getUnderusedBudgets();

        BigDecimal fts = balance.subtract(upcomingBills).add(remainingBudget).add(underused);

        if (fts.compareTo(BigDecimal.ZERO) <= 0) return insights;

        String title = "Free to spend this week";
        String detail = String.format("Rs. %s available (after bills & budgets)",
            fts.setScale(0, RoundingMode.HALF_UP));

        insights.add(new InsightDto(
            "free-to-spend-week",
            title,
            detail,
            "info",
            "View Cash Flow",
            "/cashflow"
        ));

        return insights;
    }
}