package com.finmate.finmate_backend.income.transactions.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.income.transactions.dto.IncomeTransactionDtos.*;
import com.finmate.finmate_backend.income.transactions.service.IncomeTransactionService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/income-transactions")
@RequiredArgsConstructor
public class IncomeTransactionController {

    private final IncomeTransactionService incomeTransactionService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<Page<IncomeTransactionItem>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactions(user, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(incomeTransactionService.getTransactionById(user, id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<IncomeTransactionItem>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactionsByDateRange(user, startDate, endDate));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<IncomeTransactionItem>> getTransactionsByAccount(
            @PathVariable Long accountId,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactionsByAccount(user, accountId));
    }

    @GetMapping("/income-source/{incomeSourceId}")
    public ResponseEntity<List<IncomeTransactionItem>> getTransactionsByIncomeSource(
            @PathVariable Long incomeSourceId,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactionsByIncomeSource(user, incomeSourceId));
    }

    @GetMapping("/from-date")
    public ResponseEntity<List<IncomeTransactionItem>> getTransactionsFromDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactionsFromDate(user, fromDate));
    }

    @GetMapping("/to-date")
    public ResponseEntity<List<IncomeTransactionItem>> getTransactionsToDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactionsToDate(user, toDate));
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @RequestBody CreateIncomeTransactionRequest request,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(incomeTransactionService.createTransaction(user, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(
            @PathVariable Long id,
            @RequestBody UpdateIncomeTransactionRequest request,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(incomeTransactionService.updateTransaction(user, id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        try {
            incomeTransactionService.deleteTransaction(user, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<IncomeTransactionSummary> getTransactionSummary(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactionSummary(user));
    }

    @GetMapping("/summary/date-range")
    public ResponseEntity<IncomeTransactionSummary> getTransactionSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTransactionSummaryByDateRange(user, startDate, endDate));
    }

    @GetMapping("/summary/monthly/{year}")
    public ResponseEntity<Map<String, java.math.BigDecimal>> getMonthlyIncomeForYear(
            @PathVariable int year,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getMonthlyIncomeForYear(user, year));
    }

    @GetMapping("/summary/month/{year}/{month}")
    public ResponseEntity<java.math.BigDecimal> getTotalAmountByMonth(
            @PathVariable int year,
            @PathVariable int month,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTotalAmountByMonth(user, year, month));
    }

    @GetMapping("/summary/year/{year}")
    public ResponseEntity<java.math.BigDecimal> getTotalAmountByYear(
            @PathVariable int year,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return ResponseEntity.ok(incomeTransactionService.getTotalAmountByYear(user, year));
    }
}
