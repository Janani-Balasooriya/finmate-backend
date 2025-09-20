package com.finmate.finmate_backend.families.repository;

import com.finmate.finmate_backend.families.model.FamilyInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyInviteRepository extends JpaRepository<FamilyInvite, Long> {
    
    Optional<FamilyInvite> findByToken(String token);
}
