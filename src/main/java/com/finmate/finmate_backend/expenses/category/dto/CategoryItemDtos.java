package com.finmate.finmate_backend.expenses.category.dto;

public class CategoryItemDtos {

    public record CategorySummary(
        Long id,
        String name
    ) {}

    public record CategoryItemSummary(
        Long id,
        String name
    ) {}
}