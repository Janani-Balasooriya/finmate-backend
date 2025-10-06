package com.finmate.finmate_backend.expenses.category.repository;

import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.expenses.category.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryItemRepository extends JpaRepository<CategoryItem, Long> {

    List<CategoryItem> findByExpenseCategoryAndDeletedFalse(ExpenseCategory expenseCategory);

    List<CategoryItem> findByExpenseCategoryAndDeletedFalseOrderByNameAsc(ExpenseCategory expenseCategory);
}