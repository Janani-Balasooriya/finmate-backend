package com.finmate.finmate_backend.expenses.transactions.respository;

import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseTransactionRepository extends JpaRepository<ExpenseTransaction, Long> {

    @Query("select coalesce(sum(et.amount), 0) from ExpenseTransaction et where et.user = :user and et.transactionDate between :start and :end")
    BigDecimal sumByUserAndDateBetween(@Param("user") User user,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("select coalesce(sum(et.amount), 0) from ExpenseTransaction et where et.user = :user and et.categoryItem.expenseCategory.id = :categoryId and et.transactionDate between :start and :end")
    BigDecimal sumByUserAndCategoryAndDateBetween(@Param("user") User user,
                                                  @Param("categoryId") Long categoryId,
                                                  @Param("start") LocalDate start,
                                                  @Param("end") LocalDate end);
}


