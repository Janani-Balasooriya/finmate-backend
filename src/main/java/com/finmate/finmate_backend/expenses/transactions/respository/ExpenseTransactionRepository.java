package com.finmate.finmate_backend.expenses.transactions.respository;

import com.finmate.finmate_backend.expenses.transactions.model.ExpenseTransaction;
import com.finmate.finmate_backend.families.model.Family;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query("select coalesce(sum(et.amount), 0) from ExpenseTransaction et where et.user.families = :family and et.categoryItem.expenseCategory.id = :categoryId and et.transactionDate between :start and :end")
    BigDecimal sumByFamilyAndCategoryAndDateBetween(@Param("family") Family family,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("start") LocalDate start,
                                                    @Param("end") LocalDate end);

    @Query("select coalesce(sum(et.amount), 0) from ExpenseTransaction et where et.user.families = :family and et.transactionDate between :start and :end")
    BigDecimal sumByFamilyAndDateBetween(@Param("family") Family family,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

// -------- Family-scoped finders --------
@Query("select et from ExpenseTransaction et where et.user.families.id = :familyId order by et.transactionDate desc, et.id desc")
Page<ExpenseTransaction> findByFamilyOrderByDateDesc(Long familyId, Pageable pageable);

@Query("select et from ExpenseTransaction et where et.user.families.id = :familyId and et.id = :id")
Optional<ExpenseTransaction> findByFamilyAndId(Long familyId, Long id);

@Query("select et from ExpenseTransaction et where et.user.families.id = :familyId and et.transactionDate between :start and :end order by et.transactionDate desc")
List<ExpenseTransaction> findByFamilyAndDateBetween(Long familyId, LocalDate start, LocalDate end);

@Query("select et from ExpenseTransaction et where et.user.families.id = :familyId and et.account.id = :accountId order by et.transactionDate desc")
List<ExpenseTransaction> findByFamilyAndAccountId(Long familyId, Long accountId);

@Query("select et from ExpenseTransaction et where et.user.families.id = :familyId and et.categoryItem.expenseCategory.id = :categoryId order by et.transactionDate desc")
List<ExpenseTransaction> findByFamilyAndCategoryId(Long familyId, Long categoryId);

@Query("select et from ExpenseTransaction et where et.user.families.id = :familyId and et.transactionDate >= :fromDate order by et.transactionDate desc")
List<ExpenseTransaction> findByFamilyAndDateFrom(Long familyId, LocalDate fromDate);

@Query("select et from ExpenseTransaction et where et.user.families.id = :familyId and et.transactionDate <= :toDate order by et.transactionDate desc")
List<ExpenseTransaction> findByFamilyAndDateTo(Long familyId, LocalDate toDate);

// summaries (family versions)
@Query("select coalesce(sum(et.amount),0) from ExpenseTransaction et where et.user.families.id = :familyId")
BigDecimal getTotalAmountByFamily(Long familyId);

@Query("select coalesce(sum(et.amount),0) from ExpenseTransaction et where et.user.families.id = :familyId and et.transactionDate between :start and :end")
BigDecimal getTotalAmountByFamilyAndDateRange(Long familyId, LocalDate start, LocalDate end);

@Query("select count(et) from ExpenseTransaction et where et.user.families.id = :familyId")
Long getTransactionCountByFamily(Long familyId);

@Query("select count(et) from ExpenseTransaction et where et.user.families.id = :familyId and et.transactionDate between :start and :end")
Long getTransactionCountByFamilyAndDateRange(Long familyId, LocalDate start, LocalDate end);

@Query("select min(et.transactionDate) from ExpenseTransaction et where et.user.families.id = :familyId")
LocalDate getEarliestDateByFamily(Long familyId);

@Query("select max(et.transactionDate) from ExpenseTransaction et where et.user.families.id = :familyId")
LocalDate getLatestDateByFamily(Long familyId);
}


