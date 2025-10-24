package com.finmate.finmate_backend.goals.repository;

import com.finmate.finmate_backend.goals.model.Goal;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    // Basic CRUD operations provided by JpaRepository
}