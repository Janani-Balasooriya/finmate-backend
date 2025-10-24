package com.finmate.finmate_backend.goals.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalDtos {

    public record GoalDto(
            Long id,
            String name,
            BigDecimal targetAmount,
            LocalDate deadline,
            String status,
            LocalDate createdAt,
            BigDecimal totalSaved,
            int progress
    ) {}

    public record CreateGoalRequest(
            String name,
            BigDecimal targetAmount,
            LocalDate deadline
    ) {}

    public record UpdateGoalRequest(
            String name,
            BigDecimal targetAmount,
            LocalDate deadline,
            String status
    ) {}

    public record GoalContributionDto(
            Long id,
            BigDecimal amount,
            LocalDate createdAt,
            String userName,
            String accountName
    ) {}

    public record CreateGoalContributionRequest(
            BigDecimal amount,
            Long accountId
    ) {}
}