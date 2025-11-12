package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;

import java.util.List;

public interface InsightRule {
    List<InsightDto> evaluate(FamilyContext context);
}