package com.finmate.finmate_backend.accounts.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

@Entity
@Table(
        name = "currency_codes",
        uniqueConstraints = @UniqueConstraint(name = "uk_currency_codes_code", columnNames = "code")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CurrencyCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;                 // matches INT PK

    @Column(name = "code", nullable = false, length = 3)
    private String code;                // e.g., LKR, USD, EUR

    @Column(name = "exchange_rate", precision = 14, scale = 2)
    private BigDecimal exchangeRate;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist @PreUpdate
    private void normalize() {
        if (code != null) {
            code = code.trim().toUpperCase(Locale.ROOT);
        }
    }

    // Optional: helpful toString
    @Override public String toString() { return code; }
}
