package com.finmate.finmate_backend.insights.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.insights.dto.InsightDto;
import com.finmate.finmate_backend.insights.service.InsightsService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<List<InsightDto>> getInsights(Authentication authentication) {
        User user = getUser(authentication);
        Long familyId = user.getFamilies().getId();
        List<InsightDto> insights = insightsService.getInsights(familyId);
        return ResponseEntity.ok(insights);
    }
}