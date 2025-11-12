package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionsRenewingSoonRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        List<InsightDto> insights = new ArrayList<>();
        List<Subscription> upcoming = context.getUpcomingRenewals();

        if (upcoming.isEmpty()) return insights;

        int count = upcoming.size();
        BigDecimal totalDue = upcoming.stream()
            .map(Subscription::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String title = count == 1 ? "Subscription renewing soon" : count + " subscriptions renewing soon";
        String detail = String.format("Total due: Rs. %s", totalDue.setScale(0, RoundingMode.HALF_UP));

        insights.add(new InsightDto(
            "subscriptions-renewing-soon",
            title,
            detail,
            "danger",
            "View Subscriptions",
            "/subscriptions?filter=upcoming"
        ));

        return insights;
    }
}