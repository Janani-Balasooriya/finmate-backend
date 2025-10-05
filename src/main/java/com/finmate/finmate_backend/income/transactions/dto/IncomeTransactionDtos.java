package com.finmate.finmate_backend.income.transactions.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncomeTransactionDtos {

    public record IncomeTransactionItem(
            Long id,
            BigDecimal amount,
            LocalDate transactionDate,
            Long accountId,
            String accountName,
            Long incomeSourceId,
            String incomeSourceName,
            String incomeSourceColor
    ) {}

    public record CreateIncomeTransactionRequest(
            BigDecimal amount,
            LocalDate transactionDate,
            Long accountId,
            Long incomeSourceId
    ) {}

    public record UpdateIncomeTransactionRequest(
            BigDecimal amount,
            LocalDate transactionDate,
            Long accountId,
            Long incomeSourceId
    ) {}

    public record IncomeTransactionSummary(
            BigDecimal totalAmount,
            Long transactionCount,
            BigDecimal averageAmount,
            LocalDate earliestDate,
            LocalDate latestDate
    ) {}
}
