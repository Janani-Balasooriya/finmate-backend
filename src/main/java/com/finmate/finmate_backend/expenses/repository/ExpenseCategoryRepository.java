package com.finmate.finmate_backend.expenses.repository;

import com.finmate.finmate_backend.expenses.model.ExpenseCategory;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findByUserAndDeletedFalse(User user);

    Optional<ExpenseCategory> findByIdAndUser(Long id, User user);

    @Query("select coalesce(sum(ec.budgetAmount), 0) from ExpenseCategory ec where ec.user = :user and ec.deleted = false")
    java.math.BigDecimal sumActiveBudgetsByUser(@Param("user") User user);
}


