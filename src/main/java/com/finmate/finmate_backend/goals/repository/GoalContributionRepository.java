package com.finmate.finmate_backend.goals.repository;

import com.finmate.finmate_backend.goals.model.GoalContribution;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {
    List<GoalContribution> findAllByGoal_IdOrderByCreatedAtDesc(Long goalId);

    @Query("SELECT gc FROM GoalContribution gc WHERE gc.user.families.id = :familyId AND gc.account.id = :accountId")
    List<GoalContribution> findByFamilyIdAndAccountId(@Param("familyId") Long familyId, @Param("accountId") Long accountId);

    @Query("SELECT gc FROM GoalContribution gc WHERE gc.user = :user AND gc.account.id = :accountId")
    List<GoalContribution> findByUserAndAccountId(@Param("user") User user, @Param("accountId") Long accountId);
}