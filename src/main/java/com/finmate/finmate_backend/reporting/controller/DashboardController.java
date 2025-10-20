package com.finmate.finmate_backend.reporting.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.reporting.dto.DashboardDtos.*;
import com.finmate.finmate_backend.reporting.service.DashboardService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats(Authentication authentication) {
        User user = getUser(authentication);
        DashboardStats stats = dashboardService.getDashboardStats(user);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/income-expense-trend")
    public ResponseEntity<IncomeExpenseTrend> getIncomeExpenseTrend(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().value}") int year,
            Authentication authentication) {
        User user = getUser(authentication);
        IncomeExpenseTrend trend = dashboardService.getIncomeExpenseTrend(user, year);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/budget-comparison")
    public ResponseEntity<BudgetComparison> getBudgetComparison(Authentication authentication) {
        User user = getUser(authentication);
        BudgetComparison comparison = dashboardService.getBudgetComparison(user);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/cash-flow-analysis")
    public ResponseEntity<CashFlowAnalysis> getCashFlowAnalysis(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().value}") int year,
            Authentication authentication) {
        User user = getUser(authentication);
        CashFlowAnalysis analysis = dashboardService.getCashFlowAnalysis(user, year);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/category-trends")
    public ResponseEntity<CategoryTrends> getCategoryTrends(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().value}") int year,
            Authentication authentication) {
        User user = getUser(authentication);
        CategoryTrends trends = dashboardService.getCategoryTrends(user, year);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/spending-by-category")
    public ResponseEntity<SpendingByCategory> getSpendingByCategory(Authentication authentication) {
        User user = getUser(authentication);
        SpendingByCategory spending = dashboardService.getSpendingByCategory(user);
        return ResponseEntity.ok(spending);
    }
}