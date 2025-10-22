package com.finmate.finmate_backend.subscriptions.repository;

import com.finmate.finmate_backend.subscriptions.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByUser_Families_Id(Long familyId);

    Optional<Subscription> findByIdAndUser_Families_Id(Long id, Long familyId);
}
