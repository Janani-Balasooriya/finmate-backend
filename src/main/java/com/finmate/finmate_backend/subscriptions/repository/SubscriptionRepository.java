package com.finmate.finmate_backend.subscriptions.repository;

import com.finmate.finmate_backend.subscriptions.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select s from Subscription s
        where s.user.families.id = :familyId
        order by s.name asc
    """)
    List<Subscription> findByFamilyId(Long familyId);

    @Query("""
        select s from Subscription s
        where s.user.families.id = :familyId and s.id = :id
    """)
    Optional<Subscription> findByIdAndFamilyId(Long id, Long familyId);
}