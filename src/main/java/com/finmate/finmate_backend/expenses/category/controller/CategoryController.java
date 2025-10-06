package com.finmate.finmate_backend.expenses.category.controller;

import com.finmate.finmate_backend.auth.service.AuthService;
import com.finmate.finmate_backend.expenses.category.dto.CategoryDtos.*;
import com.finmate.finmate_backend.expenses.category.service.CategoryService;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getCurrentUser(email);
    }

    // ExpenseCategory endpoints
    @GetMapping
    public ResponseEntity<List<ExpenseCategoryDto>> getCategories(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(categoryService.getActiveCategories(user));
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CreateCategoryRequest request, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(categoryService.createCategory(user, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequest request, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(categoryService.updateCategory(user, id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        try {
            categoryService.deleteCategory(user, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // CategoryItem endpoints
    @GetMapping("/{categoryId}/items")
    public ResponseEntity<List<CategoryItemDto>> getCategoryItems(@PathVariable Long categoryId, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(categoryService.getCategoryItems(user, categoryId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{categoryId}/items")
    public ResponseEntity<?> createCategoryItem(@PathVariable Long categoryId, @RequestBody CreateCategoryItemRequest request, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(categoryService.createCategoryItem(user, categoryId, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{categoryId}/items/{itemId}")
    public ResponseEntity<?> updateCategoryItem(@PathVariable Long categoryId, @PathVariable Long itemId, @RequestBody UpdateCategoryItemRequest request, Authentication authentication) {
        User user = getUser(authentication);
        try {
            return ResponseEntity.ok(categoryService.updateCategoryItem(user, categoryId, itemId, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{categoryId}/items/{itemId}")
    public ResponseEntity<Void> deleteCategoryItem(@PathVariable Long categoryId, @PathVariable Long itemId, Authentication authentication) {
        User user = getUser(authentication);
        try {
            categoryService.deleteCategoryItem(user, categoryId, itemId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}