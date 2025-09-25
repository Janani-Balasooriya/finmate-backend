package com.finmate.finmate_backend.accounts.model;

import jakarta.persistence.*;
import lombok.*;

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

    @PrePersist @PreUpdate
    private void normalize() {
        if (code != null) {
            code = code.trim().toUpperCase(Locale.ROOT);
        }
    }

    // Optional: helpful toString
    @Override public String toString() { return code; }
}
