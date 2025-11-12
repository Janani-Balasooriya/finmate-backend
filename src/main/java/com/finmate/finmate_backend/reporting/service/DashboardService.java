package com.finmate.finmate_backend.reporting.service;

import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.expenses.category.repository.CategoryRepository;
import com.finmate.finmate_backend.expenses.transactions.respository.ExpenseTransactionRepository;
import com.finmate.finmate_backend.income.transactions.repository.IncomeTransactionRepository;
import com.finmate.finmate_backend.reporting.dto.DashboardDtos.*;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeTransactionRepository incomeTransactionRepository;
    private final ExpenseTransactionRepository expenseTransactionRepository;
    private final CategoryRepository categoryRepository;

    public DashboardStats getDashboardStats(User user) {
        Long familyId = user.getFamilies().getId();
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        // Get total income for current month
        BigDecimal totalIncomeThisMonth = incomeTransactionRepository.getTotalAmountByFamilyAndDateRange(
                familyId, startOfMonth, endOfMonth);
        if (totalIncomeThisMonth == null) totalIncomeThisMonth = BigDecimal.ZERO;

        // Get total expenses for current month
        BigDecimal totalExpensesThisMonth = expenseTransactionRepository.getTotalAmountByFamilyAndDateRange(
                familyId, startOfMonth, endOfMonth);
        if (totalExpensesThisMonth == null) totalExpensesThisMonth = BigDecimal.ZERO;

        // Calculate net savings
        BigDecimal netSavings = totalIncomeThisMonth.subtract(totalExpensesThisMonth);

        // Calculate savings rate (net savings / total income * 100)
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncomeThisMonth.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(totalIncomeThisMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new DashboardStats(
                totalIncomeThisMonth,
                totalExpensesThisMonth,
                netSavings,
                savingsRate
        );
    }

    public IncomeExpenseTrend getIncomeExpenseTrend(User user, int year) {
        Long familyId = user.getFamilies().getId();
        List<MonthlyTrendData> trendData = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();

            BigDecimal monthlyIncome = incomeTransactionRepository.getTotalAmountByFamilyAndDateRange(
                    familyId, startOfMonth, endOfMonth);
            BigDecimal monthlyExpenses = expenseTransactionRepository.getTotalAmountByFamilyAndDateRange(
                    familyId, startOfMonth, endOfMonth);

            String monthName = yearMonth.getMonth().name().substring(0, 3);
            trendData.add(new MonthlyTrendData(monthName, monthlyIncome, monthlyExpenses));
        }

        return new IncomeExpenseTrend(trendData);
    }

    public BudgetComparison getBudgetComparison(User user) {
        Long familyId = user.getFamilies().getId();
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<Category> categories = categoryRepository.findAllByUser_Families_IdAndDeletedFalse(familyId);
        List<BudgetComparisonData> comparisonData = new ArrayList<>();

        for (Category category : categories) {
            BigDecimal budget = category.getBudgetAmount();
            BigDecimal actual = expenseTransactionRepository.sumByFamilyAndCategoryAndDateBetween(
                    categoryRepository.findById(category.getId()).orElseThrow().getUser().getFamilies(),
                    category.getId(), startOfMonth, endOfMonth);

            String status;
            if (actual.compareTo(budget) > 0) {
                status = "over";
            } else if (actual.compareTo(budget) < 0) {
                status = "under";
            } else {
                status = "exact";
            }

            comparisonData.add(new BudgetComparisonData(
                    category.getName(),
                    budget,
                    actual,
                    status
            ));
        }

        return new BudgetComparison(comparisonData);
    }

    public CashFlowAnalysis getCashFlowAnalysis(User user, int year) {
        Long familyId = user.getFamilies().getId();
        List<CashFlowData> cashFlowData = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();

            BigDecimal monthlyIncome = incomeTransactionRepository.getTotalAmountByFamilyAndDateRange(
                    familyId, startOfMonth, endOfMonth);
            BigDecimal monthlyExpenses = expenseTransactionRepository.getTotalAmountByFamilyAndDateRange(
                    familyId, startOfMonth, endOfMonth);

            BigDecimal cashFlow = monthlyIncome.subtract(monthlyExpenses);
            cumulative = cumulative.add(cashFlow);

            String monthName = yearMonth.getMonth().name().substring(0, 3);
            cashFlowData.add(new CashFlowData(monthName, cashFlow, cumulative));
        }

        return new CashFlowAnalysis(cashFlowData);
    }

    public CategoryTrends getCategoryTrends(User user, int year) {
        Long familyId = user.getFamilies().getId();
        List<Category> categories = categoryRepository.findAllByUser_Families_IdAndDeletedFalse(familyId);
        List<CategoryTrendData> trendData = new ArrayList<>();
        List<String> categoryNames = categories.stream().map(Category::getName).toList();

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();

            java.util.Map<String, BigDecimal> monthlyCategories = new java.util.HashMap<>();
            for (Category category : categories) {
                BigDecimal spending = expenseTransactionRepository.sumByFamilyAndCategoryAndDateBetween(
                        category.getUser().getFamilies(),
                        category.getId(),
                        startOfMonth,
                        endOfMonth);
                monthlyCategories.put(category.getName(), spending);
            }

            String monthName = yearMonth.getMonth().name().substring(0, 3);
            trendData.add(new CategoryTrendData(monthName, monthlyCategories));
        }

        return new CategoryTrends(trendData, categoryNames);
    }

    public SpendingByCategory getSpendingByCategory(User user) {
        Long familyId = user.getFamilies().getId();
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<Category> categories = categoryRepository.findAllByUser_Families_IdAndDeletedFalse(familyId);
        List<SpendingByCategoryData> spendingData = new ArrayList<>();

        // Calculate total spending for the month
        BigDecimal totalSpending = BigDecimal.ZERO;
        for (Category category : categories) {
            BigDecimal categorySpending = expenseTransactionRepository.sumByFamilyAndCategoryAndDateBetween(
                    category.getUser().getFamilies(),
                    category.getId(),
                    startOfMonth,
                    endOfMonth);
            totalSpending = totalSpending.add(categorySpending);
        }

        // Calculate percentage for each category
        for (Category category : categories) {
            BigDecimal categorySpending = expenseTransactionRepository.sumByFamilyAndCategoryAndDateBetween(
                    category.getUser().getFamilies(),
                    category.getId(),
                    startOfMonth,
                    endOfMonth);

            BigDecimal percentage = BigDecimal.ZERO;
            if (totalSpending.compareTo(BigDecimal.ZERO) > 0) {
                percentage = categorySpending.divide(totalSpending, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            spendingData.add(new SpendingByCategoryData(
                    category.getName(),
                    categorySpending,
                    percentage
            ));
        }

        return new SpendingByCategory(spendingData);
    }
}