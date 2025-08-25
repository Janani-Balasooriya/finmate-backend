package com.finmate.invitations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    Optional<Invitation> findByEmail(String email);
    List<Invitation> findByStatus(Invitation.InvitationStatus status);
    List<Invitation> findByExpiresAtBefore(LocalDateTime dateTime);
}
