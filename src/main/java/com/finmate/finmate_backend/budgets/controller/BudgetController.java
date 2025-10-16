package com.finmate.finmate_backend.budgets.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.BudgetResponse;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.CreateOrUpdateBudgetRequest;
import com.finmate.finmate_backend.budgets.service.BudgetService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

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

    private Long familyIdOf(User user) { return user.getFamilies().getId(); }

    @GetMapping
    public ResponseEntity<BudgetResponse> get(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(budgetService.getBudgetsForCurrentMonth(user));
    }

    @GetMapping("/family")
    public ResponseEntity<BudgetResponse> getFamilyBudgets(Authentication authentication) {
        User user = getUser(authentication);
        Long familyId = familyIdOf(user);
        return ResponseEntity.ok(budgetService.getBudgetsForFamilyCurrentMonth(familyId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrUpdateBudgetRequest req, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(budgetService.create(user, req));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/family")
    public ResponseEntity<?> createForFamily(@RequestBody CreateOrUpdateBudgetRequest req, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(budgetService.create(user, req));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CreateOrUpdateBudgetRequest req, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(budgetService.update(user, id, req));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/family/{id}")
    public ResponseEntity<?> updateForFamily(@PathVariable Long id, @RequestBody CreateOrUpdateBudgetRequest req, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(budgetService.update(user, id, req));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        budgetService.softDelete(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/family/{id}")
    public ResponseEntity<Void> deleteForFamily(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        budgetService.softDelete(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/families/{familyId}/budgets/{id}")
    public ResponseEntity<Void> deleteByFamily(@PathVariable Long familyId, @PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        if (!user.getFamilies().getId().equals(familyId)) {
            return ResponseEntity.status(403).build();
        }
        budgetService.softDelete(user, id);
        return ResponseEntity.noContent().build();
    }
}


