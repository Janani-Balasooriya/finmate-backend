package com.finmate.finmate_backend.expenses.transactions.service;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.accounts.repository.AccountRepository;
import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.expenses.category.repository.CategoryItemRepository;
import com.finmate.finmate_backend.expenses.transactions.dto.ExpenseTransactionDtos.*;
import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.expenses.transactions.respository.ExpenseTransactionRepository;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import com.finmate.finmate_backend.subscriptions.repository.SubscriptionRepository;
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
    private final AccountRepository accountRepository;
    private final CategoryItemRepository categoryItemRepository;
    private final SubscriptionRepository subscriptionRepository;

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
        Long familyId = familyIdOf(user);

        // Validate required fields
        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (req.transactionDate() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        if (req.accountId() == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (req.categoryItemId() == null) {
            throw new IllegalArgumentException("Category item ID is required");
        }

        // Find and validate account belongs to family
        Account account = accountRepository.findByIdAndFamilyId(req.accountId(), familyId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or does not belong to your family"));

        // Find and validate category item belongs to family
        CategoryItem categoryItem = categoryItemRepository.findById(req.categoryItemId())
                .orElseThrow(() -> new IllegalArgumentException("Category item not found"));
        if (!categoryItem.getExpenseCategory().getUser().getFamilies().getId().equals(familyId)) {
            throw new IllegalArgumentException("Category item does not belong to your family");
        }

        // Find subscription if provided
        Subscription subscription = null;
        if (req.subscriptionId() != null) {
            subscription = subscriptionRepository.findByIdAndUser_Families_Id(req.subscriptionId(), familyId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found or does not belong to your family"));
        }

        // Create and save transaction
        ExpenseTransaction transaction = new ExpenseTransaction();
        transaction.setAmount(req.amount());
        transaction.setTransactionDate(req.transactionDate());
        transaction.setAccount(account);
        transaction.setCategoryItem(categoryItem);
        transaction.setUser(user);
        transaction.setSubscription(subscription);

        ExpenseTransaction saved = expenseTransactionRepository.save(transaction);
        return toItem(saved);
    }

    public ExpenseTransactionItem update(User user, Long id, UpdateExpenseTransactionRequest req) {
        Long familyId = familyIdOf(user);

        // Find existing transaction
        ExpenseTransaction transaction = expenseTransactionRepository.findByFamilyAndId(familyId, id)
                .orElseThrow(() -> new IllegalArgumentException("Expense transaction not found"));

        // Validate required fields
        if (req.amount() != null && req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (req.transactionDate() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        if (req.accountId() == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (req.categoryItemId() == null) {
            throw new IllegalArgumentException("Category item ID is required");
        }

        // Find and validate account belongs to family
        Account account = accountRepository.findByIdAndFamilyId(req.accountId(), familyId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or does not belong to your family"));

        // Find and validate category item belongs to family
        CategoryItem categoryItem = categoryItemRepository.findById(req.categoryItemId())
                .orElseThrow(() -> new IllegalArgumentException("Category item not found"));
        if (!categoryItem.getExpenseCategory().getUser().getFamilies().getId().equals(familyId)) {
            throw new IllegalArgumentException("Category item does not belong to your family");
        }

        // Find subscription if provided
        Subscription subscription = null;
        if (req.subscriptionId() != null) {
            subscription = subscriptionRepository.findByIdAndUser_Families_Id(req.subscriptionId(), familyId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found or does not belong to your family"));
        }

        // Update transaction
        if (req.amount() != null) transaction.setAmount(req.amount());
        if (req.transactionDate() != null) transaction.setTransactionDate(req.transactionDate());
        if (req.accountId() != null) transaction.setAccount(account);
        if (req.categoryItemId() != null) transaction.setCategoryItem(categoryItem);
        if (req.subscriptionId() != null) transaction.setSubscription(subscription);

        ExpenseTransaction saved = expenseTransactionRepository.save(transaction);
        return toItem(saved);
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
            transaction.getCategoryItem().getExpenseCategory().getId(),
            transaction.getCategoryItem().getExpenseCategory().getName(),
            transaction.getCategoryItem().getId(),
            transaction.getCategoryItem().getName(),
            transaction.getCategoryItem().getExpenseCategory().getColor(),
            transaction.getSubscription() != null ? transaction.getSubscription().getId() : null,
            transaction.getSubscription() != null ? transaction.getSubscription().getName() : null
        );
    }
}