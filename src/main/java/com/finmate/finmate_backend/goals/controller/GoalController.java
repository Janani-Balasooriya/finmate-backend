package com.finmate.finmate_backend.goals.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.goals.dto.GoalDtos.*;
import com.finmate.finmate_backend.goals.service.GoalService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<List<GoalDto>> getGoals(Authentication authentication) {
        try {
            User user = getUser(authentication);
            List<GoalDto> goals = goalService.getGoals(user);
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDto> getGoal(Authentication authentication, @PathVariable Long id) {
        try {
            User user = getUser(authentication);
            GoalDto goal = goalService.getGoal(user, id);
            return ResponseEntity.ok(goal);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(Authentication authentication,
                                             @RequestBody CreateGoalRequest request) {
        try {
            User user = getUser(authentication);
            GoalDto created = goalService.createGoal(user, request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> updateGoal(Authentication authentication,
                                             @PathVariable Long id,
                                             @RequestBody UpdateGoalRequest request) {
        try {
            User user = getUser(authentication);
            GoalDto updated = goalService.updateGoal(user, id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(Authentication authentication, @PathVariable Long id) {
        try {
            User user = getUser(authentication);
            goalService.deleteGoal(user, id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{goalId}/contributions")
    public ResponseEntity<List<GoalContributionDto>> getGoalContributions(Authentication authentication,
                                                                          @PathVariable Long goalId) {
        try {
            User user = getUser(authentication);
            List<GoalContributionDto> contributions = goalService.getGoalContributions(user, goalId);
            return ResponseEntity.ok(contributions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{goalId}/contributions")
    public ResponseEntity<GoalContributionDto> addContribution(Authentication authentication,
                                                               @PathVariable Long goalId,
                                                               @RequestBody CreateGoalContributionRequest request) {
        try {
            User user = getUser(authentication);
            GoalContributionDto contribution = goalService.addContribution(user, goalId, request);
            return ResponseEntity.ok(contribution);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{goalId}/contributions/{contributionId}")
    public ResponseEntity<Void> deleteContribution(Authentication authentication,
                                                  @PathVariable Long goalId,
                                                  @PathVariable Long contributionId) {
        try {
            User user = getUser(authentication);
            goalService.deleteContribution(user, goalId, contributionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{goalId}/total-saved")
    public ResponseEntity<BigDecimal> getTotalSaved(Authentication authentication, @PathVariable Long goalId) {
        try {
            User user = getUser(authentication);
            BigDecimal total = goalService.getTotalSaved(user, goalId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
}