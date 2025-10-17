package com.finmate.finmate_backend.budgets.service;

import com.finmate.finmate_backend.budgets.dto.BudgetDtos.BudgetItem;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.BudgetResponse;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.BudgetSummary;
import com.finmate.finmate_backend.budgets.dto.BudgetDtos.CreateOrUpdateBudgetRequest;
import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.expenses.category.repository.CategoryRepository;
import com.finmate.finmate_backend.expenses.transactions.respository.ExpenseTransactionRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final CategoryRepository expenseCategoryRepository;
    private final ExpenseTransactionRepository expenseTransactionRepository;

    public BudgetResponse getBudgetsForCurrentMonth(User user) {
        YearMonth now = YearMonth.now();
        LocalDate start = now.atDay(1);
        LocalDate end = now.atEndOfMonth();

        List<Category> categories = expenseCategoryRepository.findByUser_FamiliesAndDeletedFalse(user.getFamilies());

        List<BudgetItem> items = categories.stream().map(cat -> {
            BigDecimal spent = expenseTransactionRepository
                    .sumByFamilyAndCategoryAndDateBetween(user.getFamilies(), cat.getId(), start, end);
            BigDecimal budget = cat.getBudgetAmount() == null ? BigDecimal.ZERO : cat.getBudgetAmount();
            BigDecimal remaining = budget.subtract(spent);
            int percent = budget.compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100)).divide(budget, 0, java.math.RoundingMode.HALF_UP).intValue()
                    : 0;
            return new BudgetItem(cat.getId(), cat.getName(), cat.getIcon(), cat.getColor(), budget, spent, remaining, percent);
        }).toList();

        BigDecimal totalBudget = categories.stream()
                .map(Category::getBudgetAmount)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = expenseTransactionRepository
                .sumByFamilyAndDateBetween(user.getFamilies(), start, end);
        BigDecimal remaining = totalBudget.subtract(totalSpent);
        int percentUsed = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 0, java.math.RoundingMode.HALF_UP).intValue()
                : 0;

        BudgetSummary summary = new BudgetSummary(totalBudget, totalSpent, remaining, percentUsed);
        return new BudgetResponse(summary, items);
    }

    public BudgetItem create(User user, CreateOrUpdateBudgetRequest req) {
        if (expenseCategoryRepository.existsByUserAndNameIgnoreCaseAndDeletedFalse(user, req.name())) {
            throw new IllegalArgumentException("A budget category with this name already exists");
        }
        Category category = new Category();
        category.setUser(user);
        category.setName(req.name());
        category.setIcon(req.icon());
        category.setColor(req.color());
        category.setBudgetAmount(req.budgetAmount() == null ? BigDecimal.ZERO : req.budgetAmount());
        category.setDeleted(false);
        Category saved = expenseCategoryRepository.save(category);

        // Fresh item with zero spent for current month
        return new BudgetItem(saved.getId(), saved.getName(), saved.getIcon(), saved.getColor(), saved.getBudgetAmount(), BigDecimal.ZERO, saved.getBudgetAmount(), 0);
    }

    public BudgetItem update(User user, Long id, CreateOrUpdateBudgetRequest req) {
        Category category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget category not found"));
        if (req.name() != null) {
            String newName = req.name();
            if (!newName.equalsIgnoreCase(category.getName())
                    && expenseCategoryRepository.existsByUserAndNameIgnoreCaseAndDeletedFalse(user, newName)) {
                throw new IllegalArgumentException("A budget category with this name already exists");
            }
            category.setName(newName);
        }
        if (req.icon() != null) category.setIcon(req.icon());
        if (req.color() != null) category.setColor(req.color());
        if (req.budgetAmount() != null) category.setBudgetAmount(req.budgetAmount());
        Category saved = expenseCategoryRepository.save(category);

        // Recompute current month spent
        YearMonth now = YearMonth.now();
        BigDecimal spent = expenseTransactionRepository.sumByFamilyAndCategoryAndDateBetween(user.getFamilies(), saved.getId(), now.atDay(1), now.atEndOfMonth());
        BigDecimal remaining = saved.getBudgetAmount().subtract(spent);
        int percent = saved.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.multiply(BigDecimal.valueOf(100)).divide(saved.getBudgetAmount(), 0, java.math.RoundingMode.HALF_UP).intValue()
                : 0;
        return new BudgetItem(saved.getId(), saved.getName(), saved.getIcon(), saved.getColor(), saved.getBudgetAmount(), spent, remaining, percent);
    }

    public BudgetResponse getBudgetsForFamilyCurrentMonth(Long familyId) {
        YearMonth now = YearMonth.now();
        LocalDate start = now.atDay(1);
        LocalDate end = now.atEndOfMonth();

        List<Category> categories = expenseCategoryRepository.findAllByUser_Families_IdAndDeletedFalse(familyId);

        List<BudgetItem> items = categories.stream().map(cat -> {
            BigDecimal spent = expenseTransactionRepository
                    .sumByFamilyAndCategoryAndDateBetween(cat.getUser().getFamilies(), cat.getId(), start, end);
            BigDecimal budget = cat.getBudgetAmount() == null ? BigDecimal.ZERO : cat.getBudgetAmount();
            BigDecimal remaining = budget.subtract(spent);
            int percent = budget.compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100)).divide(budget, 0, java.math.RoundingMode.HALF_UP).intValue()
                    : 0;
            return new BudgetItem(cat.getId(), cat.getName(), cat.getIcon(), cat.getColor(), budget, spent, remaining, percent);
        }).toList();

        BigDecimal totalBudget = categories.stream()
                .map(Category::getBudgetAmount)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = categories.isEmpty() ? BigDecimal.ZERO :
                expenseTransactionRepository.sumByFamilyAndDateBetween(categories.get(0).getUser().getFamilies(), start, end);
        BigDecimal remaining = totalBudget.subtract(totalSpent);
        int percentUsed = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 0, java.math.RoundingMode.HALF_UP).intValue()
                : 0;

        BudgetSummary summary = new BudgetSummary(totalBudget, totalSpent, remaining, percentUsed);
        return new BudgetResponse(summary, items);
    }

    public void softDelete(User user, Long id) {
        Category category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget category not found"));
        category.setDeleted(true);
        expenseCategoryRepository.save(category);
    }
}


