package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SavingsRateMTDRule implements InsightRule {

    private static final BigDecimal TARGET_RATE = BigDecimal.valueOf(20); // 20% target

    @Override
    public List<InsightDto> evaluate(FamilyContext ctx) {
        BigDecimal income = nz(ctx.getIncomeMTD());
        BigDecimal expense = nz(ctx.getExpenseMTD());
        if (income.compareTo(BigDecimal.ZERO) <= 0) return List.of(); // no income yet → skip

        BigDecimal savings = income.subtract(expense);
        // clamp rate to [-100, 100] just to be safe
        BigDecimal rate = savings.multiply(BigDecimal.valueOf(100))
                .divide(income, 1, RoundingMode.HALF_UP);

        // Severity logic
        String severity;
        if (savings.signum() < 0) {
            severity = "danger";                 // overspending this month
        } else if (rate.compareTo(BigDecimal.valueOf(15)) >= 0) {
            severity = "success";
        } else if (rate.compareTo(BigDecimal.valueOf(5)) >= 0) {
            severity = "info";
        } else {
            severity = "warning";
        }

        // Guidance to hit target
        LocalDate now = ctx.getNow();
        int daysLeft = now.lengthOfMonth() - now.getDayOfMonth();
        BigDecimal targetAmount = income.multiply(TARGET_RATE)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        BigDecimal gap = targetAmount.subtract(savings); // can be negative if already above target

        String title = "Savings rate this month: " + rate.setScale(0, RoundingMode.HALF_UP) + "%";

        String detail;
        if (savings.signum() < 0) {
            // overspending
            BigDecimal over = savings.abs();
            detail = String.format("Overspent by %s so far. Income %s, Expenses %s.",
                    money(over), money(income), money(expense));
        } else if (gap.signum() > 0 && daysLeft > 0) {
            // not yet at target → give per-day hint
            BigDecimal perDay = gap.divide(BigDecimal.valueOf(daysLeft), 0, RoundingMode.CEILING);
            detail = String.format("Saved %s of %s target. Need ~%s/day for %d days to hit %s%%.",
                    money(savings), money(targetAmount), money(perDay), daysLeft, TARGET_RATE);
        } else if (gap.signum() > 0) {
            // last day or no days left
            detail = String.format("Saved %s of %s target. Short by %s.",
                    money(savings), money(targetAmount), money(gap));
        } else {
            // above target
            detail = String.format(
                    "On track ✅ Saved %s — well above your %s%% savings target (≈ %s).",
                    money(savings),
                    TARGET_RATE,
                    money(targetAmount)
            );
        }

        return List.of(new InsightDto(
                "savings-rate-mtd",
                title,
                detail,
                severity,
                "View Goals",
                "/goals"
        ));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String money(BigDecimal v) {
        BigDecimal n = (v == null ? BigDecimal.ZERO : v).setScale(0, RoundingMode.HALF_UP);
        String raw = n.toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1,");
        return "Rs. " + raw;
    }
}
