package com.finmate.finmate_backend.accounts.repository;

import com.finmate.finmate_backend.accounts.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyCodeRepository extends JpaRepository<CurrencyCode, Integer> {

    List<CurrencyCode> findAllByOrderByCode();

    List<CurrencyCode> findAllByOrderById();

    boolean existsByCode(String code);

    Optional<CurrencyCode> findByCode(String code);
}

