package com.finmate.finmate_backend.accounts.dto;

import java.math.BigDecimal;

public class AccountDtos {

    public record AccountItem(
            Long id,
            String name,
            String currencyCode,
            BigDecimal openingBalance
    ) {}

    public record AccountBalance(
            Long id,
            String name,
            String currencyCode,
            BigDecimal currentBalance
    ) {}

    public record CreateAccountRequest(
            String name,
            Integer currencyCodesId,
            BigDecimal openingBalance
    ) {}

    public record UpdateAccountRequest(
            String name,
            Integer currencyCodesId,
            BigDecimal openingBalance,
            Boolean active
    ) {}

    public record CurrencyCodeDto(
            Integer id,
            String code
    ) {}
}

