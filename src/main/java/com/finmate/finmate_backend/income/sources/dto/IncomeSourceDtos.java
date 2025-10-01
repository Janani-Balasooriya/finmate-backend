package com.finmate.finmate_backend.income.sources.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class IncomeSourceDtos {

    @Schema(name = "IncomeSourceItem", description = "Public representation of an income source")
    public record IncomeSourceItem(
            @Schema(example = "1") Long id,
            @Schema(example = "Salary") String name,
            @Schema(example = "#34d399") String color
    ) {}

    @Schema(name = "CreateIncomeSourceRequest", description = "Payload to create a new income source")
    public record CreateIncomeSourceRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Freelance") String name,
            @Schema(example = "#60a5fa") String color
    ) {}
}


