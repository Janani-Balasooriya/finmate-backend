package com.finmate.finmate_backend.expenses.category.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.expenses.category.dto.CategoryItemDtos.*;
import com.finmate.finmate_backend.expenses.category.service.CategoryItemService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expense-categories")
@RequiredArgsConstructor
public class CategoryItemController {

    private final CategoryItemService expenseCategoryService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    @GetMapping
    public ResponseEntity<List<CategorySummary>> getCategories(Authentication authentication) {
        User user = getUser(authentication);
        List<CategorySummary> categories = expenseCategoryService.getCategories(user);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}/items")
    public ResponseEntity<List<CategoryItemSummary>> getItems(@PathVariable Long categoryId, Authentication authentication) {
        User user = getUser(authentication);
        List<CategoryItemSummary> items = expenseCategoryService.getItems(user, categoryId);
        return ResponseEntity.ok(items);
    }
}