package com.finmate.finmate_backend.accounts.service;

import com.finmate.finmate_backend.accounts.dto.AccountDtos.*;
import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.accounts.model.CurrencyCode;
import com.finmate.finmate_backend.accounts.repository.AccountRepository;
import com.finmate.finmate_backend.accounts.repository.CurrencyCodeRepository;
import com.finmate.finmate_backend.expenses.transactions.respository.ExpenseTransactionRepository;
import com.finmate.finmate_backend.goals.repository.GoalContributionRepository;
import com.finmate.finmate_backend.income.transactions.repository.IncomeTransactionRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final CurrencyCodeRepository currencyCodeRepository;
    private final IncomeTransactionRepository incomeTransactionRepository;
    private final ExpenseTransactionRepository expenseTransactionRepository;
    private final GoalContributionRepository goalContributionRepository;

    private Long familyIdOf(User user) { return user.getFamilies().getId(); }

    public List<AccountItem> getActiveAccounts(User user) {
        return accountRepository.findByFamilyIdAndActiveTrue(familyIdOf(user))
                .stream()
                .map(account -> new AccountItem(
                        account.getId(),
                        account.getName(),
                        account.getCurrencyCode().getCode(),
                        account.getOpeningBalance()
                ))
                .toList();
    }

    public List<AccountBalance> getAccountBalances(User user) {
        Long familyId = familyIdOf(user);
        return accountRepository.findByFamilyId(familyId)
                .stream()
                .filter(Account::isActive)
                .map(account -> {
                    // Calculate current balance from opening balance + incomes - expenses
                    BigDecimal currentBalance = calculateCurrentBalance(account, familyId);
                    return new AccountBalance(
                            account.getId(),
                            account.getName(),
                            account.getCurrencyCode().getCode(),
                            currentBalance
                    );
                })
                .toList();
    }

    public BigDecimal getAccountBalance(User user, Long accountId) {
        Account account = accountRepository.findByIdAndFamilyId(accountId, familyIdOf(user))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return calculateCurrentBalance(account, familyIdOf(user));
    }

    public AccountItem createAccount(User user, CreateAccountRequest request) {
        if (accountRepository.existsByUser_Families_IdAndNameIgnoreCaseAndActiveTrue(familyIdOf(user), request.name())) {
            throw new IllegalArgumentException("An account with this name already exists");
        }

        CurrencyCode currencyCode = currencyCodeRepository.findById(request.currencyCodesId())
                .orElseThrow(() -> new IllegalArgumentException("Currency code not found"));

        Account account = new Account();
        account.setUser(user);
        account.setName(request.name());
        account.setCurrencyCode(currencyCode);
        account.setOpeningBalance(request.openingBalance() != null ? request.openingBalance() : BigDecimal.ZERO);
        account.setActive(true);

        Account saved = accountRepository.save(account);
        return new AccountItem(
                saved.getId(),
                saved.getName(),
                saved.getCurrencyCode().getCode(),
                saved.getOpeningBalance()
        );
    }

    public AccountItem updateAccount(User user, Long id, UpdateAccountRequest request) {
        Account account = accountRepository.findByIdAndFamilyId(id, familyIdOf(user))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (request.name() != null) {
            if (!request.name().equalsIgnoreCase(account.getName()) &&
                    accountRepository.existsByUser_Families_IdAndNameIgnoreCaseAndActiveTrue(familyIdOf(user), request.name())) {
                throw new IllegalArgumentException("An account with this name already exists");
            }
            account.setName(request.name());
        }

        if (request.currencyCodesId() != null) {
            CurrencyCode currencyCode = currencyCodeRepository.findById(request.currencyCodesId())
                    .orElseThrow(() -> new IllegalArgumentException("Currency code not found"));
            account.setCurrencyCode(currencyCode);
        }

        if (request.openingBalance() != null) {
            account.setOpeningBalance(request.openingBalance());
        }

        if (request.active() != null) {
            account.setActive(request.active());
        }

        Account saved = accountRepository.save(account);
        return new AccountItem(
                saved.getId(),
                saved.getName(),
                saved.getCurrencyCode().getCode(),
                saved.getOpeningBalance()
        );
    }

    public void archiveAccount(User user, Long id) {
        Account account = accountRepository.findByIdAndFamilyId(id, familyIdOf(user))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setActive(false);
        accountRepository.save(account);
    }

    private BigDecimal calculateCurrentBalance(Account account, Long familyId) {
        // Calculate current balance: opening_balance + total_income - total_expenses - total_goal_contributions

        BigDecimal openingBalance = account.getOpeningBalance();
        System.out.println("=== Account Balance Calculation for Account: " + account.getName() + " (ID: " + account.getId() + ") ===");
        System.out.println("Opening Balance: " + openingBalance);

        // Sum all income transactions to this account (from account owner's family)
        BigDecimal totalIncome = incomeTransactionRepository
                .findByFamilyIdAndAccountId(familyId, account.getId())
                .stream()
                .map(income -> income.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Total Income: " + totalIncome);

        // Sum all expense transactions from this account (from account owner's family)
        BigDecimal totalExpenses = expenseTransactionRepository
                .findByFamilyAndAccountId(familyId, account.getId())
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Total Expenses: " + totalExpenses);

        // Sum all goal contributions made from this account (from account owner's family)
        BigDecimal totalGoalContributions = goalContributionRepository
                .findByFamilyIdAndAccountId(familyId, account.getId())
                .stream()
                .map(contribution -> contribution.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Total Goal Contributions: " + totalGoalContributions);

        // Calculate final balance: opening + income - expenses - goal contributions
        BigDecimal finalBalance = openingBalance
                .add(totalIncome)
                .subtract(totalExpenses)
                .subtract(totalGoalContributions);

        System.out.println("Final Balance Calculation: " + openingBalance + " + " + totalIncome + " - " + totalExpenses + " - " + totalGoalContributions + " = " + finalBalance);
        System.out.println("=== End Balance Calculation ===\n");

        return finalBalance;
    }
}

