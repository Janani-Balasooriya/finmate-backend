package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UncategorizedTransactionsRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        List<InsightDto> insights = new ArrayList<>();
        LocalDate weekAgo = context.getNow().minusDays(7);

        List<ExpenseTransaction> uncategorized = context.getRecentExpenses().stream()
            .filter(tx -> tx.getTransactionDate().isAfter(weekAgo))
            .filter(tx -> tx.getCategoryItem() == null ||
                     tx.getCategoryItem().getExpenseCategory() == null ||
                     "Uncategorized".equals(tx.getCategoryItem().getExpenseCategory().getName()))
            .collect(Collectors.toList());

        if (uncategorized.isEmpty()) return insights;

        int count = uncategorized.size();
        insights.add(new InsightDto(
            "uncategorized-transactions",
            count == 1 ? "1 uncategorized transaction" : count + " uncategorized transactions",
            "Recent transactions need categorization",
            "warning",
            "Categorize Transactions",
            "/transactions?filter=uncategorized"
        ));

        return insights;
    }
}