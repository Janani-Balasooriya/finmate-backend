package com.finmate.finmate_backend.accounts.controller;

import com.finmate.finmate_backend.accounts.dto.AccountDtos.*;
import com.finmate.finmate_backend.accounts.model.CurrencyCode;
import com.finmate.finmate_backend.accounts.service.AccountService;
import com.finmate.finmate_backend.accounts.service.CurrencyCodeService;
import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CurrencyCodeService currencyCodeService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<List<AccountItem>> getAccounts(
            @RequestParam(defaultValue = "true") boolean active,
            Authentication authentication) {
        User user = getUser(authentication);
        if (active) {
            return ResponseEntity.ok(accountService.getActiveAccounts(user));
        }
        // For now, return active accounts only. Can be extended to return all accounts
        return ResponseEntity.ok(accountService.getActiveAccounts(user));
    }

    @GetMapping("/balances")
    public ResponseEntity<List<AccountBalance>> getAccountBalances(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(accountService.getAccountBalances(user));
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(accountService.createAccount(user, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @RequestBody UpdateAccountRequest request, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(accountService.updateAccount(user, id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveAccount(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        try {
            accountService.archiveAccount(user, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/currency-codes")
    public ResponseEntity<List<CurrencyCodeDto>> getCurrencyCodes() {
        List<CurrencyCode> currencyCodes = currencyCodeService.getAllCurrencyCodes();
        List<CurrencyCodeDto> dtos = currencyCodes.stream()
                .map(cc -> new CurrencyCodeDto(cc.getId(), cc.getCode()))
                .toList();
        return ResponseEntity.ok(dtos);
    }
}

