package com.finmate.finmate_backend.goals.service;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.accounts.repository.AccountRepository;
import com.finmate.finmate_backend.goals.dto.GoalDtos.*;
import com.finmate.finmate_backend.goals.model.Goal;
import com.finmate.finmate_backend.goals.model.GoalContribution;
import com.finmate.finmate_backend.goals.model.enums.GoalStatus;
import com.finmate.finmate_backend.goals.repository.GoalContributionRepository;
import com.finmate.finmate_backend.goals.repository.GoalRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final AccountRepository accountRepository;

    private void validateUser(User user) {
        if (user == null) throw new IllegalArgumentException("User is null");
        if (user.getFamilies() == null) throw new IllegalArgumentException("User family is null");
    }

    public List<GoalDto> getGoals(User user) {
        validateUser(user);
        return goalRepository.findByFamilies(user.getFamilies())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public GoalDto getGoal(User user, Long id) {
        validateUser(user);

        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        // Verify the goal belongs to the user's family
        if (!goal.getFamilies().getId().equals(user.getFamilies().getId())) {
            throw new IllegalArgumentException("Goal not found");
        }

        return toDto(goal);
    }

    @Transactional
    public GoalDto createGoal(User user, CreateGoalRequest request) {
        validateUser(user);

        Goal goal = new Goal();
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setDeadline(request.deadline());
        goal.setStatus(GoalStatus.active);
        goal.setFamilies(user.getFamilies());

        Goal saved = goalRepository.save(goal);
        return toDto(saved);
    }

    @Transactional
    public GoalDto updateGoal(User user, Long id, UpdateGoalRequest request) {
        validateUser(user);

        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        // Verify the goal belongs to the user's family
        if (!goal.getFamilies().getId().equals(user.getFamilies().getId())) {
            throw new IllegalArgumentException("Goal not found");
        }

        if (request.name() != null) goal.setName(request.name());
        if (request.targetAmount() != null) goal.setTargetAmount(request.targetAmount());
        if (request.deadline() != null) goal.setDeadline(request.deadline());
        if (request.status() != null) goal.setStatus(GoalStatus.valueOf(request.status()));

        Goal saved = goalRepository.save(goal);
        return toDto(saved);
    }

    @Transactional
    public void deleteGoal(User user, Long id) {
        validateUser(user);

        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        // Verify the goal belongs to the user's family
        if (!goal.getFamilies().getId().equals(user.getFamilies().getId())) {
            throw new IllegalArgumentException("Goal not found");
        }

        goalRepository.delete(goal);
    }

    public List<GoalContributionDto> getGoalContributions(User user, Long goalId) {
        validateUser(user);

        // Verify goal exists and belongs to user's family
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        if (!goal.getFamilies().getId().equals(user.getFamilies().getId())) {
            throw new IllegalArgumentException("Goal not found");
        }

        return goalContributionRepository.findAllByGoal_IdOrderByCreatedAtDesc(goalId)
                .stream()
                .map(this::toContributionDto)
                .toList();
    }

    @Transactional
    public GoalContributionDto addContribution(User user, Long goalId, CreateGoalContributionRequest request) {
        validateUser(user);

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        // Verify the goal belongs to the user's family
        if (!goal.getFamilies().getId().equals(user.getFamilies().getId())) {
            throw new IllegalArgumentException("Goal not found");
        }

        Account account = accountRepository.findByIdAndFamilyId(request.accountId(), user.getFamilies().getId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        GoalContribution contribution = new GoalContribution();
        contribution.setUser(user);
        contribution.setGoal(goal);
        contribution.setAccount(account);
        contribution.setAmount(request.amount());

        GoalContribution saved = goalContributionRepository.save(contribution);
        return toContributionDto(saved);
    }

    @Transactional
    public void deleteContribution(User user, Long goalId, Long contributionId) {
        validateUser(user);

        // Verify goal exists and belongs to user's family
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        if (!goal.getFamilies().getId().equals(user.getFamilies().getId())) {
            throw new IllegalArgumentException("Goal not found");
        }

        // Find and verify the contribution belongs to the user
        GoalContribution contribution = goalContributionRepository.findById(contributionId)
                .orElseThrow(() -> new IllegalArgumentException("Contribution not found"));

        // Verify the contribution belongs to the user
        if (!contribution.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot delete another user's contribution");
        }

        // Verify the contribution belongs to the goal
        if (!contribution.getGoal().getId().equals(goalId)) {
            throw new IllegalArgumentException("Contribution does not belong to goal");
        }

        goalContributionRepository.delete(contribution);
    }

    public BigDecimal getTotalSaved(User user, Long goalId) {
        validateUser(user);

        // Verify goal exists and belongs to user's family
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        if (!goal.getFamilies().getId().equals(user.getFamilies().getId())) {
            throw new IllegalArgumentException("Goal not found");
        }

        // Calculate total from contributions
        return goalContributionRepository.findAllByGoal_IdOrderByCreatedAtDesc(goalId)
                .stream()
                .map(GoalContribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GoalDto toDto(Goal goal) {
        BigDecimal totalSaved = goalContributionRepository.findAllByGoal_IdOrderByCreatedAtDesc(goal.getId())
                .stream()
                .map(GoalContribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? totalSaved.divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).intValue()
                : 0;

        return new GoalDto(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getDeadline(),
                goal.getStatus().name(),
                goal.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                totalSaved,
                progress
        );
    }

    private GoalContributionDto toContributionDto(GoalContribution contribution) {
        return new GoalContributionDto(
                contribution.getId(),
                contribution.getAmount(),
                contribution.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                contribution.getUser().getFirstName() + " " + contribution.getUser().getLastName(),
                contribution.getAccount().getName()
        );
    }
}