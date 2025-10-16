package com.finmate.finmate_backend.expenses.transactions.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseTransactionDtos {

    /**
     * Public representation of an expense transaction row.
     * Includes denormalized names/colors for easy frontend rendering.
     */
    public record ExpenseTransactionItem(
            Long id,
            BigDecimal amount,
            LocalDate transactionDate,

            Long accountId,
            String accountName,

            Long categoryId,
            String categoryName,
            Long categoryItemId,
            String categoryItemName,
            String categoryColor,   // e.g. "bg-orange-500" or hex; adjust to your data

            Long subscriptionId,    // may be null
            String subscriptionName // may be null
    ) {}

    /**
     * Payload to create a new expense transaction.
     * User is taken from the auth context; not part of the payload.
     * subscriptionId is optional (nullable).
     */
    public record CreateExpenseTransactionRequest(
            BigDecimal amount,
            LocalDate transactionDate,
            Long accountId,
            Long categoryItemId,
            Long subscriptionId // optional
    ) {}

    /**
     * Payload to update an existing expense transaction.
     * Any field may be null to indicate "no change" (if you implement partial update),
     * or treat it as a full update if your service requires all fields.
     */
    public record UpdateExpenseTransactionRequest(
            BigDecimal amount,
            LocalDate transactionDate,
            Long accountId,
            Long categoryItemId,
            Long subscriptionId // optional
    ) {}

    /**
     * Simple summary block; expand as needed (e.g., byCategory/byAccount).
     */
    public record ExpenseTransactionSummary(
            BigDecimal totalAmount,
            Long transactionCount,
            BigDecimal averageAmount,
            LocalDate earliestDate,
            LocalDate latestDate
    ) {}
}
