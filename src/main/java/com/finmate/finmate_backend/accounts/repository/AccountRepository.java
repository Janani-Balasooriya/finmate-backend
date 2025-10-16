package com.finmate.finmate_backend.accounts.repository;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserAndActiveTrue(User user);

    List<Account> findByUser(User user);

    Optional<Account> findByIdAndUser(Long id, User user);

    boolean existsByUserAndNameIgnoreCaseAndActiveTrue(User user, String name);

// -------- Family-scoped finders --------
@Query("""
  select a from Account a
  where a.user.families.id = :familyId and a.active = true
  order by a.name asc
""")
List<Account> findByFamilyIdAndActiveTrue(Long familyId);

@Query("""
  select a from Account a
  where a.user.families.id = :familyId
""")
List<Account> findByFamilyId(Long familyId);

@Query("""
  select a from Account a
  where a.user.families.id = :familyId and a.id = :id
""")
Optional<Account> findByIdAndFamilyId(Long id, Long familyId);

boolean existsByUser_Families_IdAndNameIgnoreCaseAndActiveTrue(Long familyId, String name);
}
