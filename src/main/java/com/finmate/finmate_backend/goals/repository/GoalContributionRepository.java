package com.finmate.finmate_backend.goals.repository;

import com.finmate.finmate_backend.goals.model.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {
    List<GoalContribution> findAllByGoal_IdOrderByCreatedAtDesc(Long goalId);
}