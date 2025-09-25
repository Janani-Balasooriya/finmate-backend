package com.finmate.finmate_backend.budgets.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.BudgetResponse;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.CreateOrUpdateBudgetRequest;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.BudgetItem;
import com.finmate.finmate_backend.budgets.service.BudgetService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<BudgetResponse> get(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(budgetService.getBudgetsForCurrentMonth(user));
    }

    @PostMapping
    public ResponseEntity<BudgetItem> create(@RequestBody CreateOrUpdateBudgetRequest req, Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(budgetService.create(user, req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetItem> update(@PathVariable Long id, @RequestBody CreateOrUpdateBudgetRequest req, Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(budgetService.update(user, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        budgetService.softDelete(user, id);
        return ResponseEntity.noContent().build();
    }
}


