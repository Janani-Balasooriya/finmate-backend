package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DuplicateChargesRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        List<InsightDto> insights = new ArrayList<>();
        List<ExpenseTransaction> recent = context.getRecentExpenses();
        Set<String> flagged = new HashSet<>();

        for (int i = 0; i < recent.size(); i++) {
            ExpenseTransaction tx1 = recent.get(i);
            if (tx1.getCategoryItem() == null || tx1.getCategoryItem().getExpenseCategory() == null) continue;

            String merchant1 = tx1.getCategoryItem().getExpenseCategory().getName();
            BigDecimal amount1 = tx1.getAmount();
            LocalDate date1 = tx1.getTransactionDate();

            for (int j = i + 1; j < recent.size(); j++) {
                ExpenseTransaction tx2 = recent.get(j);
                if (tx2.getCategoryItem() == null || tx2.getCategoryItem().getExpenseCategory() == null) continue;

                String merchant2 = tx2.getCategoryItem().getExpenseCategory().getName();
                BigDecimal amount2 = tx2.getAmount();
                LocalDate date2 = tx2.getTransactionDate();

                if (!merchant1.equals(merchant2)) continue;

                long hoursBetween = Math.abs(ChronoUnit.HOURS.between(date1.atStartOfDay(), date2.atStartOfDay()));
                long daysBetween = hoursBetween / 24;

                // Skip if transactions are likely legitimate recurring charges
                // Allow monthly patterns (25-35 days apart) with similar amounts
                if (daysBetween >= 25 && daysBetween <= 35) {
                    continue; // Likely monthly bill - not a duplicate
                }

                // Skip if more than 7 days apart
                if (hoursBetween > 168) continue; // 7 days = 168 hours

                // For very close transactions (same day or next day), be more strict
                BigDecimal diff = amount1.subtract(amount2).abs();
                BigDecimal threshold = amount1.multiply(BigDecimal.valueOf(0.05)); // 5% tolerance
                if (diff.compareTo(threshold) > 0) continue;

                // Flag as potential duplicate
                String pairId = Math.min(tx1.getId(), tx2.getId()) + "-" + Math.max(tx1.getId(), tx2.getId());
                if (flagged.add(pairId)) {
                    insights.add(new InsightDto(
                        "duplicate-charge-" + pairId,
                        "Possible duplicate charge",
                        String.format("Similar charge from %s: Rs. %s on %s and %s",
                            merchant1,
                            amount1.setScale(0, java.math.RoundingMode.HALF_UP),
                            date1, date2),
                        "warning",
                        "Check Transactions",
                        "/transactions?possibleDuplicate=true"
                    ));
                }
            }
        }

        return insights;
    }
}