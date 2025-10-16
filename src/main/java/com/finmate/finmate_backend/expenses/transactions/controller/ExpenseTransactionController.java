package com.finmate.finmate_backend.expenses.transactions.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.expenses.transactions.dto.ExpenseTransactionDtos.*;
import com.finmate.finmate_backend.expenses.transactions.service.ExpenseTransactionService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/expense-transactions")
@RequiredArgsConstructor
public class ExpenseTransactionController {

    private final ExpenseTransactionService expenseTransactionService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseTransactionItem>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = getUser(authentication);
        Page<ExpenseTransactionItem> transactions = expenseTransactionService.getTransactions(user, page, size);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseTransactionItem> getById(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        ExpenseTransactionItem transaction = expenseTransactionService.getById(user, id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        User user = getUser(authentication);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        var transactions = expenseTransactionService.getByDateRange(user, start, end);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<?> getByAccount(@PathVariable Long accountId, Authentication authentication) {
        User user = getUser(authentication);
        var transactions = expenseTransactionService.getByAccount(user, accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategory(@PathVariable Long categoryId, Authentication authentication) {
        User user = getUser(authentication);
        var transactions = expenseTransactionService.getByCategory(user, categoryId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/from-date")
    public ResponseEntity<?> getFromDate(@RequestParam String fromDate, Authentication authentication) {
        User user = getUser(authentication);
        LocalDate from = LocalDate.parse(fromDate);
        var transactions = expenseTransactionService.getFromDate(user, from);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/to-date")
    public ResponseEntity<?> getToDate(@RequestParam String toDate, Authentication authentication) {
        User user = getUser(authentication);
        LocalDate to = LocalDate.parse(toDate);
        var transactions = expenseTransactionService.getToDate(user, to);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateExpenseTransactionRequest req, Authentication authentication) {
        User user = getUser(authentication);
        try {
            var transaction = expenseTransactionService.create(user, req);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateExpenseTransactionRequest req, Authentication authentication) {
        User user = getUser(authentication);
        try {
            var transaction = expenseTransactionService.update(user, id, req);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        expenseTransactionService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseTransactionSummary> getSummary(Authentication authentication) {
        User user = getUser(authentication);
        var summary = expenseTransactionService.getSummary(user);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/date-range")
    public ResponseEntity<ExpenseTransactionSummary> getSummaryByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        User user = getUser(authentication);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        var summary = expenseTransactionService.getSummaryByDateRange(user, start, end);
        return ResponseEntity.ok(summary);
    }
}