package com.finmate.finmate_backend.income.sources.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.income.sources.dto.IncomeSourceDtos.CreateIncomeSourceRequest;
import com.finmate.finmate_backend.income.sources.dto.IncomeSourceDtos.IncomeSourceItem;
import com.finmate.finmate_backend.income.sources.dto.IncomeSourceDtos.UpdateIncomeSourceRequest;
import com.finmate.finmate_backend.income.sources.service.IncomeSourceService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/income-sources")
@RequiredArgsConstructor
public class IncomeSourceController {

    private final IncomeSourceService incomeSourceService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<List<IncomeSourceItem>> get(
            @RequestParam(name = "active", required = false, defaultValue = "false") boolean active,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        if (active) {
            return ResponseEntity.ok(incomeSourceService.getActive(user));
        }
        // For now, return active by default if unspecified
        return ResponseEntity.ok(incomeSourceService.getActive(user));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateIncomeSourceRequest req, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(incomeSourceService.create(user, req));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncomeSource(@PathVariable Long id, @RequestBody UpdateIncomeSourceRequest request, Authentication authentication) {
        User user = getUser(authentication);
        try{
            return ResponseEntity.ok(incomeSourceService.updateIncomeSource(user, id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncomeSource(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        try {
            incomeSourceService.deleteIncomeSource(user, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}


