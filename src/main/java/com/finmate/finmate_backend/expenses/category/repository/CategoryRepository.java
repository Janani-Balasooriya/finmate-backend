package com.finmate.finmate_backend.expenses.category.repository;

import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.families.model.Family;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserAndDeletedFalse(User user);

    List<Category> findByUser_FamiliesAndDeletedFalse(Family family);

    List<Category> findAllByUser_Families_IdAndDeletedFalse(Long familyId);

    Optional<Category> findByIdAndUser_Families_Id(Long id, Long familyId);

    @Query("select coalesce(sum(ec.budgetAmount), 0) from Category ec where ec.user.families.id = :familyId and ec.deleted = false")
    java.math.BigDecimal sumActiveBudgetsByFamilyId(@Param("familyId") Long familyId);

    boolean existsByUser_Families_IdAndNameIgnoreCaseAndDeletedFalse(Long familyId, String name);
}


