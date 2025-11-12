package com.finmate.finmate_backend.insights.service;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.accounts.repository.AccountRepository;
import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.expenses.category.repository.CategoryRepository;
import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.expenses.transactions.respository.ExpenseTransactionRepository;
import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import com.finmate.finmate_backend.insights.rule.*;
import com.finmate.finmate_backend.income.transactions.model.IncomeTransaction;
import com.finmate.finmate_backend.income.transactions.repository.IncomeTransactionRepository;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import com.finmate.finmate_backend.subscriptions.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightsService {

    private final ExpenseTransactionRepository expenseRepo;
    private final IncomeTransactionRepository incomeRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;

    private final List<InsightRule> rules = Arrays.asList(
        new BudgetOverPaceRule(),
        new SubscriptionsRenewingSoonRule(),
        new SavingsRateMTDRule(),
        new DuplicateChargesRule(),
        new SubscriptionPriceJumpRule()
//        new FreeToSpendRule()
    );

    public List<InsightDto> getInsights(Long familyId) {
        try {
            FamilyContext context = buildFamilyContext(familyId);
            List<InsightDto> insights = rules.stream()
                .flatMap(rule -> rule.evaluate(context).stream())
                .sorted(this::compareBySeverity)
                .limit(6)
                .collect(Collectors.toList());
            return insights;
        } catch (Exception e) {
            // Log the error and return empty list on error to prevent 500 errors
            System.err.println("Error generating insights for family " + familyId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private FamilyContext buildFamilyContext(Long familyId) {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        LocalDate sixtyDaysAgo = now.minusDays(60);
        LocalDate sevenDaysFromNow = now.plusDays(7);

        // MTD totals
        BigDecimal incomeMTD = incomeRepo.getTotalAmountByFamilyAndDateRange(familyId, monthStart, monthEnd);
        BigDecimal expenseMTD = expenseRepo.getTotalAmountByFamilyAndDateRange(familyId, monthStart, monthEnd);

        // Recent transactions
        List<ExpenseTransaction> recentExpenses = expenseRepo.findByFamilyAndDateBetween(familyId, sixtyDaysAgo, now);
        List<IncomeTransaction> recentIncomes = incomeRepo.findByFamilyIdAndTransactionDateBetween(familyId, sixtyDaysAgo, now);

        // Budgets with MTD spend
        List<Category> categories = categoryRepo.findAllByUser_Families_IdAndDeletedFalse(familyId);
        Map<Category, BigDecimal> budgetsWithMTDSpend = categories.stream()
            .collect(Collectors.toMap(
                cat -> cat,
                cat -> expenseRepo.sumByFamilyAndCategoryAndDateBetween(
                    cat.getUser().getFamilies(), cat.getId(), monthStart, monthEnd)
            ));

        // Upcoming renewals
        List<Subscription> upcomingRenewals = subscriptionRepo.findAllByUser_Families_Id(familyId).stream()
            .filter(sub -> sub.getNextChargeDate() != null && !sub.getNextChargeDate().isAfter(sevenDaysFromNow))
            .collect(Collectors.toList());

        // Checking balance
        BigDecimal checkingBalance = accountRepo.findByFamilyIdAndActiveTrue(familyId).stream()
            .map(acc -> acc.getOpeningBalance() != null ? acc.getOpeningBalance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Upcoming bills (7 days)
        BigDecimal upcomingBills7d = upcomingRenewals.stream()
            .map(Subscription::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Month remaining budget
        BigDecimal totalBudget = categories.stream()
            .map(cat -> cat.getBudgetAmount() != null ? cat.getBudgetAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthRemainingBudget = totalBudget.subtract(expenseMTD);

        // Underused budgets (spent < 80% of budget)
        BigDecimal underusedBudgets = budgetsWithMTDSpend.entrySet().stream()
            .filter(entry -> {
                BigDecimal budget = entry.getKey().getBudgetAmount();
                BigDecimal spent = entry.getValue();
                return budget != null && spent.divide(budget, 2, java.math.RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(0.8)) < 0;
            })
            .map(entry -> entry.getKey().getBudgetAmount().subtract(entry.getValue()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FamilyContext.builder()
            .familyId(familyId)
            .now(now)
            .incomeMTD(incomeMTD != null ? incomeMTD : BigDecimal.ZERO)
            .expenseMTD(expenseMTD != null ? expenseMTD : BigDecimal.ZERO)
            .recentExpenses(recentExpenses)
            .recentIncomes(recentIncomes)
            .budgetsWithMTDSpend(budgetsWithMTDSpend)
            .upcomingRenewals(upcomingRenewals)
            .checkingBalance(checkingBalance)
            .upcomingBills7d(upcomingBills7d)
            .monthRemainingBudget(monthRemainingBudget)
            .underusedBudgets(underusedBudgets)
            .build();
    }

    private int compareBySeverity(InsightDto a, InsightDto b) {
        Map<String, Integer> severityOrder = Map.of(
            "danger", 1,
            "warning", 2,
            "info", 3,
            "success", 4
        );
        return Integer.compare(severityOrder.get(a.severity()), severityOrder.get(b.severity()));
    }
}