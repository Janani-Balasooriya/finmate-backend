package com.finmate.finmate_backend.accounts.repository;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    List<Account> findByUserAndActiveTrue(User user);
    
    List<Account> findByUser(User user);
    
    Optional<Account> findByIdAndUser(Long id, User user);
    
    boolean existsByUserAndNameIgnoreCaseAndActiveTrue(User user, String name);
}
