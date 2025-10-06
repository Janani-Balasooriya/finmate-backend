package com.finmate.finmate_backend.expenses.category.dto;

import java.math.BigDecimal;

public class CategoryDtos {

    public record ExpenseCategoryDto(
            Long id,
            String name,
            String icon,
            String color,
            BigDecimal budgetAmount
    ) {}

    public record CategoryItemDto(
            Long id,
            String name
    ) {}

    public record CreateCategoryRequest(
            String name,
            String icon,
            String color,
            BigDecimal budgetAmount
    ) {}

    public record UpdateCategoryRequest(
            String name,
            String icon,
            String color,
            BigDecimal budgetAmount
    ) {}

    public record CreateCategoryItemRequest(
            String name
    ) {}

    public record UpdateCategoryItemRequest(
            String name
    ) {}
}