package com.finmate.finmate_backend.accounts.service;

import com.finmate.finmate_backend.accounts.model.CurrencyCode;
import com.finmate.finmate_backend.accounts.repository.CurrencyCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyCodeService implements CommandLineRunner {

    private final CurrencyCodeRepository currencyCodeRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${exchangerate.api.key:}")
    private String exchangeRateApiKey;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedCurrencyCodes();
        // Check if we need initial exchange rate update
        checkAndUpdateInitialExchangeRates();
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

    private void checkAndUpdateInitialExchangeRates() {
        try {
            // Check if any currency has null exchange rate (first time setup)
            List<CurrencyCode> currencies = currencyCodeRepository.findAll();
            boolean needsInitialUpdate = currencies.stream()
                    .anyMatch(c -> c.getExchangeRate() == null);

            if (needsInitialUpdate && exchangeRateApiKey != null && !exchangeRateApiKey.trim().isEmpty()) {
                log.info("Detected first-time setup, updating exchange rates immediately");
                updateExchangeRates();
            } else if (needsInitialUpdate) {
                log.warn("Exchange rates not initialized and API key not configured");
            }
        } catch (Exception e) {
            log.error("Error during initial exchange rate check", e);
        }
    }
    
    public List<CurrencyCode> getAllCurrencyCodes() {
        return currencyCodeRepository.findAllByOrderById();
    }

    @Transactional
    public void updateExchangeRates() {
        try {
            // Check if we need to update (once per day)
            Instant lastUpdate = getLastUpdateTime();
            Instant now = Instant.now();

            if (lastUpdate != null && lastUpdate.isAfter(now.minus(1, ChronoUnit.DAYS))) {
                log.info("Exchange rates were updated recently, skipping update");
                return;
            }

            // Fetch exchange rates from API
            String apiUrl = "https://v6.exchangerate-api.com/v6/" + exchangeRateApiKey + "/latest/USD";
            log.info("Fetching exchange rates from: {}", apiUrl);

            ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String baseCurrency = (String) responseBody.get("base_code");

                if (!"USD".equals(baseCurrency)) {
                    log.warn("Expected USD as base currency, got: {}", baseCurrency);
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Double> rates = (Map<String, Double>) responseBody.get("conversion_rates");

                if (rates != null) {
                    // Update exchange rates in database
                    List<CurrencyCode> currencyCodes = currencyCodeRepository.findAll();

                    for (CurrencyCode currencyCode : currencyCodes) {
                        Object rateObj = rates.get(currencyCode.getCode());
                        if (rateObj != null) {
                            Double rate;
                            if (rateObj instanceof Integer) {
                                rate = ((Integer) rateObj).doubleValue();
                            } else if (rateObj instanceof Double) {
                                rate = (Double) rateObj;
                            } else if (rateObj instanceof Number) {
                                rate = ((Number) rateObj).doubleValue();
                            } else {
                                log.warn("Unexpected rate type for {}: {}", currencyCode.getCode(), rateObj.getClass());
                                continue;
                            }

                            currencyCode.setExchangeRate(BigDecimal.valueOf(rate).setScale(6, RoundingMode.HALF_UP));
                            currencyCode.setUpdatedAt(now);
                            currencyCodeRepository.save(currencyCode);
                            log.debug("Updated {} exchange rate to: {}", currencyCode.getCode(), rate);
                        } else {
                            log.warn("No exchange rate found for currency: {}", currencyCode.getCode());
                        }
                    }

                    log.info("Successfully updated exchange rates for {} currencies", currencyCodes.size());
                } else {
                    log.error("No conversion_rates found in API response");
                }
            } else {
                log.error("Failed to fetch exchange rates. Status: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error updating exchange rates", e);
        }
    }

    private Instant getLastUpdateTime() {
        List<CurrencyCode> currencies = currencyCodeRepository.findAll();
        return currencies.stream()
                .filter(c -> c.getUpdatedAt() != null)
                .map(CurrencyCode::getUpdatedAt)
                .max(Instant::compareTo)
                .orElse(null);
    }

    /**
     * Scheduled task to update exchange rates once per day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledUpdateExchangeRates() {
        log.info("Starting scheduled exchange rate update");
        if (exchangeRateApiKey == null || exchangeRateApiKey.trim().isEmpty()) {
            log.warn("Exchange rate API key not configured, skipping update");
            return;
        }
        updateExchangeRates();
    }
}

