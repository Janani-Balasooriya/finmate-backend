package com.finmate.finmate_backend.insights.rule;

import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.model.FamilyContext;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubscriptionsRenewingSoonRule implements InsightRule {

    @Override
    public List<InsightDto> evaluate(FamilyContext context) {
        var upcoming = context.getUpcomingRenewals(); // List<Subscription> due within next 7 days
        if (upcoming == null || upcoming.isEmpty()) return List.of();

        LocalDate today = context.getNow();

        // Sort by next renewal date
        upcoming.sort(Comparator.comparing(Subscription::getNextChargeDate));

        int count = upcoming.size();
        BigDecimal totalDue = upcoming.stream()
                .map(Subscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Soonest days-left to set severity
        long minDays = upcoming.stream()
                .mapToLong(s -> daysUntil(today, s.getNextChargeDate()))
                .min()
                .orElse(7);

        String severity = (minDays <= 3) ? "danger" : "warning";

        // Build a concise preview of the first 2–3 items
        String preview = upcoming.stream()
                .limit(3)
                .map(s -> {
                    String name = safeName(s);
                    long d = daysUntil(today, s.getNextChargeDate());
                    String when = switch ((int) d) {
                        case 0 -> "today";
                        case 1 -> "tomorrow";
                        default -> "in " + d + " days";
                    };
                    return name + " (" + when + ")";
                })
                .collect(Collectors.joining(", "));

        // “and N more” if needed
        if (count > 3) preview += " … and " + (count - 3) + " more";

        String title = (count == 1)
                ? "1 subscription renewing soon"
                : count + " subscriptions renewing soon";

        String detail = "Due soon: " + preview + ". Total: " + money(totalDue);

        return List.of(new InsightDto(
                "subscriptions-renewing-soon",
                title,
                detail,
                severity,
                "Review subscriptions",
                "/subscriptions?filter=upcoming"
        ));
    }

    private static long daysUntil(LocalDate from, LocalDate to) {
        return Math.max(0, ChronoUnit.DAYS.between(from, to));
    }

    private static String safeName(Subscription s) {
        if (s.getName() != null && !s.getName().isBlank()) return s.getName();
//        if (s.getMerchant() != null && !s.getMerchant().isBlank()) return s.getMerchant();
        return "Subscription";
    }

    private static String money(BigDecimal v) {
        BigDecimal n = v == null ? BigDecimal.ZERO : v.setScale(0, RoundingMode.HALF_UP);
        // "Rs. 12,345"
        String raw = n.toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1,");
        return "Rs. " + raw;
    }
}
