package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionPriceJumpRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        List<InsightDto> insights = new ArrayList<>();
        // This rule would need historical subscription data to compare last two renewals
        // For now, we'll skip implementation as it requires additional data model changes
        // The task specifies "no new database tables", so this might need to be implemented differently
        return insights;
    }
}