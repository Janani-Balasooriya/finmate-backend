package com.finmate.finmate_backend.expenses.category.repository;

import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.expenses.category.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryItemRepository extends JpaRepository<CategoryItem, Long> {

    List<CategoryItem> findByExpenseCategoryAndDeletedFalse(ExpenseCategory expenseCategory);

    List<CategoryItem> findByExpenseCategoryAndDeletedFalseOrderByNameAsc(ExpenseCategory expenseCategory);

@Query("select ci from CategoryItem ci where ci.expenseCategory.id = :categoryId and ci.expenseCategory.user.families.id = :familyId and ci.deleted = false order by ci.name asc")
List<CategoryItem> findItemsByCategoryIdAndFamilyId(Long categoryId, Long familyId);
}