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
public class SavingsRateMTDRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        List<InsightDto> insights = new ArrayList<>();
        BigDecimal income = context.getIncomeMTD();
        BigDecimal expense = context.getExpenseMTD();

        if (income.compareTo(BigDecimal.ZERO) <= 0) return insights;

        BigDecimal savings = income.subtract(expense);
        BigDecimal rate = savings.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        String severity;
        if (rate.compareTo(BigDecimal.valueOf(15)) >= 0) {
            severity = "success";
        } else if (rate.compareTo(BigDecimal.valueOf(5)) >= 0) {
            severity = "info";
        } else {
            severity = "warning";
        }

        String title = "Savings rate this month: " + rate.setScale(1, RoundingMode.HALF_UP) + "%";
        String detail = String.format("Income: Rs. %s, Expenses: Rs. %s, Saved: Rs. %s",
            income.setScale(0, RoundingMode.HALF_UP),
            expense.setScale(0, RoundingMode.HALF_UP),
            savings.setScale(0, RoundingMode.HALF_UP));

        insights.add(new InsightDto(
            "savings-rate-mtd",
            title,
            detail,
            severity,
            "View Goals",
            "/goals"
        ));

        return insights;
    }
}