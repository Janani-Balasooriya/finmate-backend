package com.finmate.finmate_backend.accounts.service;

import com.finmate.finmate_backend.accounts.dto.AccountDtos.*;
import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.accounts.model.CurrencyCode;
import com.finmate.finmate_backend.accounts.repository.AccountRepository;
import com.finmate.finmate_backend.accounts.repository.CurrencyCodeRepository;
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

    public List<AccountItem> getActiveAccounts(User user) {
        return accountRepository.findByUserAndActiveTrue(user)
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
        return accountRepository.findByUser(user)
                .stream()
                .filter(Account::isActive)
                .map(account -> {
                    // Calculate current balance from opening balance + incomes - expenses
                    BigDecimal currentBalance = calculateCurrentBalance(account);
                    return new AccountBalance(
                            account.getId(),
                            account.getName(),
                            account.getCurrencyCode().getCode(),
                            currentBalance
                    );
                })
                .toList();
    }

    public AccountItem createAccount(User user, CreateAccountRequest request) {
        if (accountRepository.existsByUserAndNameIgnoreCaseAndActiveTrue(user, request.name())) {
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
        Account account = accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (request.name() != null) {
            if (!request.name().equalsIgnoreCase(account.getName()) &&
                    accountRepository.existsByUserAndNameIgnoreCaseAndActiveTrue(user, request.name())) {
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
        Account account = accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setActive(false);
        accountRepository.save(account);
    }

    private BigDecimal calculateCurrentBalance(Account account) {
        // For now, return opening balance. In a real implementation, you would:
        // 1. Sum all income transactions for this account
        // 2. Sum all expense transactions for this account
        // 3. Return opening_balance + total_income - total_expenses
        // This would require additional repository methods to query income/expense transactions
        return account.getOpeningBalance();
    }
}

