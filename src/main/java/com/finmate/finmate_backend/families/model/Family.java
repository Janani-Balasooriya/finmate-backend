package com.finmate.finmate_backend.families.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "families")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB default is CURRENT_TIMESTAMP, but we also enable JPA auditing
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonIgnore
    private Instant createdAt;

    @Size(max = 5)
    @Column(name = "base_currency_code", length = 5)
    private String baseCurrencyCode;

}
