package com.finmate.finmate_backend.reporting.service;

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

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeTransactionRepository incomeTransactionRepository;
    private final ExpenseTransactionRepository expenseTransactionRepository;

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
}