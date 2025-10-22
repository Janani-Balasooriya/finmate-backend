package com.finmate.finmate_backend.subscriptions.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.subscriptions.dto.SubscriptionDtos.*;
import com.finmate.finmate_backend.subscriptions.service.SubscriptionService;
import com.finmate.finmate_backend.users.model.User;
import com.finmate.finmate_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionItemDto>> getSubscriptions(Authentication authentication) {
        try {
            User user = getUser(authentication);
            return ResponseEntity.ok(subscriptionService.getSubscriptions(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionItemDto> getSubscription(Authentication authentication, @PathVariable Long id) {
        try {
            User user = getUser(authentication);
            return ResponseEntity.ok(subscriptionService.getSubscription(user, id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping
    public ResponseEntity<SubscriptionItemDto> createSubscription(Authentication authentication,
                                                                  @RequestBody CreateSubscriptionRequest request) {
        try {
            User user = getUser(authentication);
            return ResponseEntity.ok(subscriptionService.createWithCategoryItem(user, request));
        } catch (IllegalArgumentException e) {
            System.err.println("Error creating subscription: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionItemDto> updateSubscription(Authentication authentication,
                                                                  @PathVariable Long id,
                                                                  @RequestBody UpdateSubscriptionRequest request) {
        try {
            User user = getUser(authentication);
            return ResponseEntity.ok(subscriptionService.updateSubscription(user, id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(Authentication authentication, @PathVariable Long id) {
        try {
            User user = getUser(authentication);
            subscriptionService.deleteSubscription(user, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
