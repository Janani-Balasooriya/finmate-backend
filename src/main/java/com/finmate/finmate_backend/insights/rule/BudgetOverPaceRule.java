package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BudgetOverPaceRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        List<InsightDto> insights = new ArrayList<>();

        LocalDate now = context.getNow();
        int day = now.getDayOfMonth();
        int daysInMonth = now.lengthOfMonth();
        int daysLeft = daysInMonth - day;

        BigDecimal bdDay = BigDecimal.valueOf(day);
        BigDecimal bdDaysInMonth = BigDecimal.valueOf(daysInMonth);

        for (Map.Entry<Category, BigDecimal> entry : context.getBudgetsWithMTDSpend().entrySet()) {
            Category cat = entry.getKey();
            BigDecimal spentMTD = entry.getValue();
            BigDecimal limit = cat.getBudgetAmount();
            if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Expected spend by today (calendar pace)
            BigDecimal expectedByToday = limit
                    .multiply(bdDay)
                    .divide(bdDaysInMonth, 0, RoundingMode.HALF_UP);

            // Projection using daily average pace
            BigDecimal projected = (day > 0)
                    ? spentMTD.divide(bdDay, 2, RoundingMode.HALF_UP)
                    .multiply(bdDaysInMonth)
                    : spentMTD; // safe guard for day==0 (shouldn't happen)

            BigDecimal overAtMonthEnd = projected.subtract(limit);

            // Severity thresholds
            boolean danger = projected.compareTo(limit.multiply(BigDecimal.valueOf(1.05))) > 0; // >5% over
            boolean warning = !danger && spentMTD.compareTo(expectedByToday) > 0; // already ahead of pace

            if (danger || warning) {
                String title = "At risk: " + cat.getName() + " budget";

                String detail = String.format(
                        "Spent %s of %s with %d %s left. On current pace you’ll reach ~%s%s",
                        money(spentMTD),
                        money(limit),
                        daysLeft,
                        daysLeft == 1 ? "day" : "days",
                        money(projected.setScale(0, RoundingMode.HALF_UP)),
                        overAtMonthEnd.signum() > 0
                                ? String.format(" (≈ %s over).", money(overAtMonthEnd.setScale(0, RoundingMode.HALF_UP)))
                                : String.format(" (≈ %s under).", money(overAtMonthEnd.abs().setScale(0, RoundingMode.HALF_UP)))
                );

                insights.add(new InsightDto(
                        "budget-overpace-" + cat.getId(),
                        title,
                        detail,
                        danger ? "danger" : "warning",
                        "Review budget",
                        "/budgets?catId=" + cat.getId()
                ));
            }
        }
        return insights;
    }

    private String money(BigDecimal v) {
        // Rs. with thousands separator, no decimals
        return "Rs. " + v.setScale(0, RoundingMode.HALF_UP)
                .toPlainString()
                .replaceAll("(\\d)(?=(\\d{3})+$)", "$1,");
    }
}
