package com.finmate.finmate_backend.expenses.transactions.service;

import com.finmate.finmate_backend.expenses.transactions.dto.ExpenseTransactionDtos.*;
import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.expenses.transactions.respository.ExpenseTransactionRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseTransactionService {

    private final ExpenseTransactionRepository expenseTransactionRepository;

    private Long familyIdOf(User user) { return user.getFamilies().getId(); }

    public Page<ExpenseTransactionItem> getTransactions(User user, int page, int size) {
        Long familyId = familyIdOf(user);
        Pageable pageable = PageRequest.of(page, size);
        Page<ExpenseTransaction> transactions = expenseTransactionRepository.findByFamilyOrderByDateDesc(familyId, pageable);
        return transactions.map(this::toItem);
    }

    public ExpenseTransactionItem getById(User user, Long id) {
        Long familyId = familyIdOf(user);
        ExpenseTransaction transaction = expenseTransactionRepository.findByFamilyAndId(familyId, id)
                .orElseThrow(() -> new IllegalArgumentException("Expense transaction not found"));
        return toItem(transaction);
    }

    public List<ExpenseTransactionItem> getByDateRange(User user, LocalDate start, LocalDate end) {
        Long familyId = familyIdOf(user);
        List<ExpenseTransaction> transactions = expenseTransactionRepository.findByFamilyAndDateBetween(familyId, start, end);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<ExpenseTransactionItem> getByAccount(User user, Long accountId) {
        Long familyId = familyIdOf(user);
        List<ExpenseTransaction> transactions = expenseTransactionRepository.findByFamilyAndAccountId(familyId, accountId);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<ExpenseTransactionItem> getByCategory(User user, Long categoryId) {
        Long familyId = familyIdOf(user);
        List<ExpenseTransaction> transactions = expenseTransactionRepository.findByFamilyAndCategoryId(familyId, categoryId);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<ExpenseTransactionItem> getFromDate(User user, LocalDate fromDate) {
        Long familyId = familyIdOf(user);
        List<ExpenseTransaction> transactions = expenseTransactionRepository.findByFamilyAndDateFrom(familyId, fromDate);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<ExpenseTransactionItem> getToDate(User user, LocalDate toDate) {
        Long familyId = familyIdOf(user);
        List<ExpenseTransaction> transactions = expenseTransactionRepository.findByFamilyAndDateTo(familyId, toDate);
        return transactions.stream().map(this::toItem).toList();
    }

    public ExpenseTransactionItem create(User user, CreateExpenseTransactionRequest req) {
        // TODO: Implement validation and creation
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ExpenseTransactionItem update(User user, Long id, UpdateExpenseTransactionRequest req) {
        // TODO: Implement validation and update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void delete(User user, Long id) {
        Long familyId = familyIdOf(user);
        ExpenseTransaction transaction = expenseTransactionRepository.findByFamilyAndId(familyId, id)
                .orElseThrow(() -> new IllegalArgumentException("Expense transaction not found"));
        expenseTransactionRepository.delete(transaction);
    }

    public ExpenseTransactionSummary getSummary(User user) {
        Long familyId = familyIdOf(user);
        BigDecimal totalAmount = expenseTransactionRepository.getTotalAmountByFamily(familyId);
        Long transactionCount = expenseTransactionRepository.getTransactionCountByFamily(familyId);
        BigDecimal averageAmount = transactionCount > 0 ?
            totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;
        LocalDate earliestDate = expenseTransactionRepository.getEarliestDateByFamily(familyId);
        LocalDate latestDate = expenseTransactionRepository.getLatestDateByFamily(familyId);

        return new ExpenseTransactionSummary(totalAmount, transactionCount, averageAmount, earliestDate, latestDate);
    }

    public ExpenseTransactionSummary getSummaryByDateRange(User user, LocalDate start, LocalDate end) {
        Long familyId = familyIdOf(user);
        BigDecimal totalAmount = expenseTransactionRepository.getTotalAmountByFamilyAndDateRange(familyId, start, end);
        Long transactionCount = expenseTransactionRepository.getTransactionCountByFamilyAndDateRange(familyId, start, end);
        BigDecimal averageAmount = transactionCount > 0 ?
            totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        return new ExpenseTransactionSummary(totalAmount, transactionCount, averageAmount, start, end);
    }

    private ExpenseTransactionItem toItem(ExpenseTransaction transaction) {
        return new ExpenseTransactionItem(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            transaction.getAccount().getId(),
            transaction.getAccount().getName(),
            transaction.getCategoryItem().getId(),
            transaction.getCategoryItem().getName(),
            transaction.getCategoryItem().getExpenseCategory().getColor(),
            transaction.getSubscription() != null ? transaction.getSubscription().getId() : null,
            transaction.getSubscription() != null ? transaction.getSubscription().getName() : null
        );
    }
}