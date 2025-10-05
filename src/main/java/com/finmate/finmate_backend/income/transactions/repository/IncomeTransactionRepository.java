package com.finmate.finmate_backend.income.transactions.repository;

import com.finmate.finmate_backend.income.transactions.model.IncomeTransaction;
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

public interface IncomeTransactionRepository extends JpaRepository<IncomeTransaction, Long> {

    @Query("SELECT it FROM IncomeTransaction it WHERE it.user = :user ORDER BY it.transactionDate DESC, it.id DESC")
    Page<IncomeTransaction> findByUserOrderByTransactionDateDesc(@Param("user") User user, Pageable pageable);

    @Query("SELECT it FROM IncomeTransaction it WHERE it.user = :user AND it.id = :id")
    Optional<IncomeTransaction> findByUserAndId(@Param("user") User user, @Param("id") Long id);

    @Query("SELECT it FROM IncomeTransaction it WHERE it.user = :user AND it.transactionDate BETWEEN :startDate AND :endDate ORDER BY it.transactionDate DESC")
    List<IncomeTransaction> findByUserAndTransactionDateBetween(@Param("user") User user, 
                                                               @Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT it FROM IncomeTransaction it WHERE it.user = :user AND it.account.id = :accountId ORDER BY it.transactionDate DESC")
    List<IncomeTransaction> findByUserAndAccountId(@Param("user") User user, @Param("accountId") Long accountId);

    @Query("SELECT it FROM IncomeTransaction it WHERE it.user = :user AND it.incomeSource.id = :incomeSourceId ORDER BY it.transactionDate DESC")
    List<IncomeTransaction> findByUserAndIncomeSourceId(@Param("user") User user, @Param("incomeSourceId") Long incomeSourceId);

    @Query("SELECT it FROM IncomeTransaction it WHERE it.user = :user AND it.transactionDate >= :fromDate ORDER BY it.transactionDate DESC")
    List<IncomeTransaction> findByUserAndTransactionDateFrom(@Param("user") User user, @Param("fromDate") LocalDate fromDate);

    @Query("SELECT it FROM IncomeTransaction it WHERE it.user = :user AND it.transactionDate <= :toDate ORDER BY it.transactionDate DESC")
    List<IncomeTransaction> findByUserAndTransactionDateTo(@Param("user") User user, @Param("toDate") LocalDate toDate);

    // Summary queries
    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IncomeTransaction it WHERE it.user = :user")
    BigDecimal getTotalAmountByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IncomeTransaction it WHERE it.user = :user AND it.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByUserAndDateRange(@Param("user") User user, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(it) FROM IncomeTransaction it WHERE it.user = :user")
    Long getTransactionCountByUser(@Param("user") User user);

    @Query("SELECT COUNT(it) FROM IncomeTransaction it WHERE it.user = :user AND it.transactionDate BETWEEN :startDate AND :endDate")
    Long getTransactionCountByUserAndDateRange(@Param("user") User user, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(AVG(it.amount), 0) FROM IncomeTransaction it WHERE it.user = :user")
    BigDecimal getAverageAmountByUser(@Param("user") User user);

    @Query("SELECT MIN(it.transactionDate) FROM IncomeTransaction it WHERE it.user = :user")
    LocalDate getEarliestTransactionDateByUser(@Param("user") User user);

    @Query("SELECT MAX(it.transactionDate) FROM IncomeTransaction it WHERE it.user = :user")
    LocalDate getLatestTransactionDateByUser(@Param("user") User user);

    // Monthly/yearly summaries
    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IncomeTransaction it WHERE it.user = :user AND YEAR(it.transactionDate) = :year AND MONTH(it.transactionDate) = :month")
    BigDecimal getTotalAmountByUserAndMonth(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IncomeTransaction it WHERE it.user = :user AND YEAR(it.transactionDate) = :year")
    BigDecimal getTotalAmountByUserAndYear(@Param("user") User user, @Param("year") int year);

    // Check if transaction exists for user
    boolean existsByUserAndId(User user, Long id);
}
