package com.finmate.finmate_backend.income.transactions.service;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.accounts.repository.AccountRepository;
import com.finmate.finmate_backend.income.sources.model.IncomeSource;
import com.finmate.finmate_backend.income.sources.repository.IncomeSourceRepository;
import com.finmate.finmate_backend.income.transactions.dto.IncomeTransactionDtos.*;
import com.finmate.finmate_backend.income.transactions.model.IncomeTransaction;
import com.finmate.finmate_backend.income.transactions.repository.IncomeTransactionRepository;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeTransactionService {

    private final IncomeTransactionRepository incomeTransactionRepository;
    private final AccountRepository accountRepository;
    private final IncomeSourceRepository incomeSourceRepository;

    private Long familyIdOf(User user) { return user.getFamilies().getId(); }

    public Page<IncomeTransactionItem> getTransactions(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<IncomeTransaction> transactions = incomeTransactionRepository.findByFamilyIdOrderByTransactionDateDesc(familyIdOf(user), pageable);
        return transactions.map(this::toItem);
    }

    public List<IncomeTransactionItem> getTransactionsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        List<IncomeTransaction> transactions = incomeTransactionRepository.findByFamilyIdAndTransactionDateBetween(familyIdOf(user), startDate, endDate);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<IncomeTransactionItem> getTransactionsByAccount(User user, Long accountId) {
        List<IncomeTransaction> transactions = incomeTransactionRepository.findByFamilyIdAndAccountId(familyIdOf(user), accountId);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<IncomeTransactionItem> getTransactionsByIncomeSource(User user, Long incomeSourceId) {
        List<IncomeTransaction> transactions = incomeTransactionRepository.findByFamilyIdAndIncomeSourceId(familyIdOf(user), incomeSourceId);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<IncomeTransactionItem> getTransactionsFromDate(User user, LocalDate fromDate) {
        List<IncomeTransaction> transactions = incomeTransactionRepository.findByFamilyIdAndTransactionDateFrom(familyIdOf(user), fromDate);
        return transactions.stream().map(this::toItem).toList();
    }

    public List<IncomeTransactionItem> getTransactionsToDate(User user, LocalDate toDate) {
        List<IncomeTransaction> transactions = incomeTransactionRepository.findByFamilyIdAndTransactionDateTo(familyIdOf(user), toDate);
        return transactions.stream().map(this::toItem).toList();
    }

    public IncomeTransactionItem getTransactionById(User user, Long id) {
        IncomeTransaction transaction = incomeTransactionRepository.findByFamilyIdAndId(familyIdOf(user), id)
                .orElseThrow(() -> new IllegalArgumentException("Income transaction not found"));
        return toItem(transaction);
    }

    public IncomeTransactionItem createTransaction(User user, CreateIncomeTransactionRequest request) {
        validateCreateRequest(request);

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        IncomeSource incomeSource = incomeSourceRepository.findById(request.incomeSourceId())
                .orElseThrow(() -> new IllegalArgumentException("Income source not found"));

        // Verify ownership
        if (!account.getUser().getFamilies().getId().equals(familyIdOf(user))) {
            throw new IllegalArgumentException("Account does not belong to user's family");
        }
        if (!incomeSource.getUser().getFamilies().getId().equals(familyIdOf(user))) {
            throw new IllegalArgumentException("Income source does not belong to user's family");
        }

        IncomeTransaction transaction = new IncomeTransaction();
        transaction.setUser(user);
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setAccount(account);
        transaction.setIncomeSource(incomeSource);

        IncomeTransaction saved = incomeTransactionRepository.save(transaction);
        return toItem(saved);
    }

    public IncomeTransactionItem updateTransaction(User user, Long id, UpdateIncomeTransactionRequest request) {
        IncomeTransaction transaction = incomeTransactionRepository.findByFamilyIdAndId(familyIdOf(user), id)
                .orElseThrow(() -> new IllegalArgumentException("Income transaction not found"));

        if (request.amount() != null) {
            transaction.setAmount(request.amount());
        }
        if (request.transactionDate() != null) {
            transaction.setTransactionDate(request.transactionDate());
        }
        if (request.accountId() != null) {
            Account account = accountRepository.findById(request.accountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            if (!account.getUser().getFamilies().getId().equals(familyIdOf(user))) {
                throw new IllegalArgumentException("Account does not belong to user's family");
            }
            transaction.setAccount(account);
        }
        if (request.incomeSourceId() != null) {
            IncomeSource incomeSource = incomeSourceRepository.findById(request.incomeSourceId())
                    .orElseThrow(() -> new IllegalArgumentException("Income source not found"));
            if (!incomeSource.getUser().getFamilies().getId().equals(familyIdOf(user))) {
                throw new IllegalArgumentException("Income source does not belong to user's family");
            }
            transaction.setIncomeSource(incomeSource);
        }

        IncomeTransaction saved = incomeTransactionRepository.save(transaction);
        return toItem(saved);
    }

    public void deleteTransaction(User user, Long id) {
        IncomeTransaction transaction = incomeTransactionRepository.findByFamilyIdAndId(familyIdOf(user), id)
                .orElseThrow(() -> new IllegalArgumentException("Income transaction not found"));
        incomeTransactionRepository.delete(transaction);
    }

    public IncomeTransactionSummary getTransactionSummary(User user) {
        BigDecimal totalAmount = incomeTransactionRepository.getTotalAmountByFamilyId(familyIdOf(user));
        Long transactionCount = incomeTransactionRepository.getTransactionCountByFamilyId(familyIdOf(user));
        BigDecimal averageAmount = transactionCount > 0 ?
            totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;
        LocalDate earliestDate = incomeTransactionRepository.getEarliestTransactionDateByFamilyId(familyIdOf(user));
        LocalDate latestDate = incomeTransactionRepository.getLatestTransactionDateByFamilyId(familyIdOf(user));

        return new IncomeTransactionSummary(totalAmount, transactionCount, averageAmount, earliestDate, latestDate);
    }

    public IncomeTransactionSummary getTransactionSummaryByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalAmount = incomeTransactionRepository.getTotalAmountByFamilyAndDateRange(familyIdOf(user), startDate, endDate);
        Long transactionCount = incomeTransactionRepository.getTransactionCountByFamilyAndDateRange(familyIdOf(user), startDate, endDate);
        BigDecimal averageAmount = transactionCount > 0 ?
            totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        return new IncomeTransactionSummary(totalAmount, transactionCount, averageAmount, startDate, endDate);
    }

    public BigDecimal getTotalAmountByMonth(User user, int year, int month) {
        return incomeTransactionRepository.getTotalAmountByFamilyAndMonth(familyIdOf(user), year, month);
    }

    public BigDecimal getTotalAmountByYear(User user, int year) {
        return incomeTransactionRepository.getTotalAmountByFamilyAndYear(familyIdOf(user), year);
    }

    public Map<String, BigDecimal> getMonthlyIncomeForYear(User user, int year) {
        Map<String, BigDecimal> monthlyIncome = new java.util.HashMap<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal amount = incomeTransactionRepository.getTotalAmountByFamilyAndMonth(familyIdOf(user), year, month);
            monthlyIncome.put(String.format("%d-%02d", year, month), amount);
        }
        return monthlyIncome;
    }

    private void validateCreateRequest(CreateIncomeTransactionRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (request.transactionDate() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        if (request.accountId() == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (request.incomeSourceId() == null) {
            throw new IllegalArgumentException("Income source ID is required");
        }
    }

    private IncomeTransactionItem toItem(IncomeTransaction transaction) {
        return new IncomeTransactionItem(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getAccount().getId(),
                transaction.getAccount().getName(),
                transaction.getIncomeSource().getId(),
                transaction.getIncomeSource().getName(),
                transaction.getIncomeSource().getColor()
        );
    }
}
