package com.finmate.finmate_backend.subscriptions.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionDtos {

    public record SubscriptionDto(
            Long id,
            String name,
            BigDecimal amount,
            String currency,
            String billingCycle,
            Long accountId,
            String accountName,
            Integer currencyId,
            String currencyCode,
            LocalDate nextBilling,
            boolean isActive
    ) {}

    public record CreateSubscriptionRequest(
            String serviceName,
            Long categoryId,
            Long accountId,
            BigDecimal amount,
            String billingCycle,
            LocalDate nextBilling
    ) {}

    public record UpdateSubscriptionRequest(
            String name,
            BigDecimal amount,
            String billingCycle,
            Long accountId,
            Long categoryId,
            LocalDate nextBilling,
            Boolean isActive
    ) {}

    public record SubscriptionItemDto(
            Long id,
            String name,
            BigDecimal amount,
            BigDecimal amountInBaseCurrency,
            String currency,
            String baseCurrency,
            String billingCycle,
            Long accountId,
            String accountName,
            Long categoryId,
            String categoryName,
            String categoryColor,
            Long categoryItemId,
            String categoryItemName,
            LocalDate nextBilling,
            boolean isActive
    ) {}
}