package com.finmate.finmate_backend.expenses.category.service;

import com.finmate.finmate_backend.expenses.category.dto.CategoryItemDtos.*;
import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.expenses.category.repository.CategoryItemRepository;
import com.finmate.finmate_backend.expenses.category.repository.CategoryRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseCategoryService {

    private final CategoryRepository expenseCategoryRepository;
    private final CategoryItemRepository categoryItemRepository;

    private Long familyIdOf(User user) { return user.getFamilies().getId(); }

    public List<CategorySummary> getCategories(User user) {
        Long familyId = familyIdOf(user);
        List<Category> categories = expenseCategoryRepository.findAllByUser_Families_IdAndDeletedFalse(familyId);
        return categories.stream().map(cat -> new CategorySummary(cat.getId(), cat.getName())).toList();
    }

    public List<CategoryItemSummary> getItems(User user, Long categoryId) {
        Long familyId = familyIdOf(user);
        List<CategoryItem> items = categoryItemRepository.findItemsByCategoryIdAndFamilyId(categoryId, familyId);
        return items.stream().map(item -> new CategoryItemSummary(item.getId(), item.getName())).toList();
    }
}