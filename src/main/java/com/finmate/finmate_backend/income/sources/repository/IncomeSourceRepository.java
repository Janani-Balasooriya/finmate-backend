package com.finmate.finmate_backend.income.sources.repository;

import com.finmate.finmate_backend.income.sources.model.IncomeSource;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IncomeSourceRepository extends JpaRepository<IncomeSource, Long> {

    @Query("select s from IncomeSource s where s.user = :user and s.deleted = false order by s.name asc")
    List<IncomeSource> findActiveByUser(User user);

    boolean existsByUserAndNameIgnoreCaseAndDeletedFalse(User user, String name);
Optional<IncomeSource> findByIdAndUser(Long id, User user);

// -------- Family-scoped finders --------
@Query("""
select s from IncomeSource s
where s.user.families.id = :familyId
and s.deleted = false
order by s.name asc
""")
List<IncomeSource> findActiveByFamilyId(Long familyId);

boolean existsByUser_Families_IdAndNameIgnoreCaseAndDeletedFalse(Long familyId, String name);

@Query("""
select s from IncomeSource s
where s.user.families.id = :familyId
and s.id = :id
""")
Optional<IncomeSource> findByIdAndFamilyId(Long id, Long familyId);
}

