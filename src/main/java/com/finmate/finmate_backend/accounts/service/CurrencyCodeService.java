package com.finmate.finmate_backend.accounts.service;

import com.finmate.finmate_backend.accounts.model.CurrencyCode;
import com.finmate.finmate_backend.accounts.repository.CurrencyCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyCodeService implements CommandLineRunner {
    
    private final CurrencyCodeRepository currencyCodeRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedCurrencyCodes();
    }
    
    private void seedCurrencyCodes() {
        if (currencyCodeRepository.count() == 0) {
            List<String> codes = Arrays.asList("LKR", "USD", "EUR", "GBP", "INR");
            
            for (String code : codes) {
                if (!currencyCodeRepository.existsByCode(code)) {
                    CurrencyCode currencyCode = CurrencyCode.builder()
                            .code(code)
                            .build();
                    currencyCodeRepository.save(currencyCode);
                }
            }
        }
    }
    
    public List<CurrencyCode> getAllCurrencyCodes() {
        return currencyCodeRepository.findAllByOrderByCode();
    }
}

