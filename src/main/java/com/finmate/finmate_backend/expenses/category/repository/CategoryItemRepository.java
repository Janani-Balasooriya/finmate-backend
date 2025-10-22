package com.finmate.finmate_backend.expenses.category.repository;

import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.expenses.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryItemRepository extends JpaRepository<CategoryItem, Long> {

    List<CategoryItem> findByExpenseCategoryAndDeletedFalse(Category expenseCategory);

    List<CategoryItem> findByExpenseCategoryAndDeletedFalseOrderByNameAsc(Category expenseCategory);

@Query("select ci from CategoryItem ci where ci.expenseCategory.id = :categoryId and ci.expenseCategory.user.families.id = :familyId and ci.deleted = false order by ci.name asc")
List<CategoryItem> findItemsByCategoryIdAndFamilyId(Long categoryId, Long familyId);

    boolean existsByExpenseCategory_IdAndExpenseCategory_User_Families_IdAndNameIgnoreCase(Long aLong, Long familyId, String s);
}