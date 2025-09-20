package com.finmate.finmate_backend.families.repository;

import com.finmate.finmate_backend.families.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
}
