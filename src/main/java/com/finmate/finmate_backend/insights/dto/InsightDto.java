package com.finmate.finmate_backend.insights.dto;

public record InsightDto(
        String id,
        String title,
        String detail,
        String severity, // danger, warning, info, success
        String ctaLabel,
        String ctaRoute
) {}