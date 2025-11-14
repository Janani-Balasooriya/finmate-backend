package com.finmate.finmate_backend.accounts.service;

import com.finmate.finmate_backend.accounts.model.CurrencyCode;
import com.finmate.finmate_backend.accounts.repository.CurrencyCodeRepository;
import com.finmate.finmate_backend.users.model.User;
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

    /**
     * Get the base currency code for a user's family
     * @param user The user
     * @return Base currency code (defaults to "USD" if not set)
     */
    public String getBaseCurrencyForUser(User user) {
        if (user == null || user.getFamilies() == null) {
            return "USD"; // Default fallback
        }
        String baseCurrency = user.getFamilies().getBaseCurrencyCode();
        return (baseCurrency != null && !baseCurrency.trim().isEmpty()) ? baseCurrency : "USD";
    }

    /**
     * Update exchange rates for a user (uses USD as base for system-wide consistency)
     * @param user The user triggering the update
     */
    @Transactional
    public void updateExchangeRatesForUser(User user) {
        log.info("Updating exchange rates for user {} (using USD base)", user.getId());
        updateExchangeRates(); // Uses USD as base for system-wide consistency
    }


    /**
     * Convert amount from one currency to another using database exchange rates
     * @param amount The amount to convert
     * @param fromCurrencyCode The source currency code (e.g., "USD")
     * @param toCurrencyCode The target currency code (e.g., "LKR")
     * @return Converted amount
     */
    public BigDecimal convertCurrency(BigDecimal amount, String fromCurrencyCode, String toCurrencyCode) {
        if (amount == null || fromCurrencyCode == null || toCurrencyCode == null) {
            throw new IllegalArgumentException("Amount and currency codes cannot be null");
        }

        if (fromCurrencyCode.equals(toCurrencyCode)) {
            return amount; // No conversion needed
        }

        // Get exchange rates from database
        CurrencyCode fromCurrency = currencyCodeRepository.findByCode(fromCurrencyCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + fromCurrencyCode));

        CurrencyCode toCurrency = currencyCodeRepository.findByCode(toCurrencyCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + toCurrencyCode));

        BigDecimal fromRate = fromCurrency.getExchangeRate();
        BigDecimal toRate = toCurrency.getExchangeRate();

        if (fromRate == null || toRate == null) {
            throw new IllegalStateException("Exchange rates not available for conversion");
        }

        // Convert: amount in fromCurrency * (toRate / fromRate)
        // Since rates are stored as "units of currency per 1 USD", we need to:
        // 1. Convert fromCurrency amount to USD: amount / fromRate
        // 2. Convert USD to toCurrency: (amount / fromRate) * toRate

        BigDecimal amountInUSD = amount.divide(fromRate, 6, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amountInUSD.multiply(toRate);

        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convert amount to LKR if it's not already in LKR
     * @param amount The amount to convert
     * @param currencyCode The current currency code
     * @return Amount in LKR
     */
    public BigDecimal convertToLKR(BigDecimal amount, String currencyCode) {
        if ("LKR".equals(currencyCode)) {
            return amount; // Already in LKR
        }
        return convertCurrency(amount, currencyCode, "LKR");
    }

    @Transactional
    public void updateExchangeRates() {
        updateExchangeRates("USD"); // Default to USD for backward compatibility
    }

    @Transactional
    public void updateExchangeRates(String baseCurrency) {
        try {
            // Check if we need to update (once per day)
            Instant lastUpdate = getLastUpdateTime();
            Instant now = Instant.now();

            if (lastUpdate != null && lastUpdate.isAfter(now.minus(1, ChronoUnit.DAYS))) {
                log.info("Exchange rates were updated recently, skipping update");
                return;
            }

            // ALWAYS use USD as base currency for system-wide consistency
            // since we have a shared database for all families
            String actualBaseCurrency = "USD";
            String apiUrl = "https://v6.exchangerate-api.com/v6/" + exchangeRateApiKey + "/latest/" + actualBaseCurrency;
            log.info("Fetching exchange rates from: {} (base: {})", apiUrl, actualBaseCurrency);

            ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String responseBaseCurrency = (String) responseBody.get("base_code");

                if (!actualBaseCurrency.equalsIgnoreCase(responseBaseCurrency)) {
                    log.warn("Expected {} as base currency, got: {}", actualBaseCurrency, responseBaseCurrency);
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

