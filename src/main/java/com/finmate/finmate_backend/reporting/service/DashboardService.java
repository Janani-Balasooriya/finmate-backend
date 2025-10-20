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
import java.util.Map;

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

        // Get total expenses for current month
        BigDecimal totalExpensesThisMonth = expenseTransactionRepository.getTotalAmountByFamilyAndDateRange(
                familyId, startOfMonth, endOfMonth);

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
}