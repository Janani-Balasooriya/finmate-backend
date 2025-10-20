package com.finmate.finmate_backend.expenses.category.service;

import com.finmate.finmate_backend.expenses.category.dto.CategoryDtos.*;
import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.expenses.category.repository.CategoryItemRepository;
import com.finmate.finmate_backend.expenses.category.repository.CategoryRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository expenseCategoryRepository;
    private final CategoryItemRepository categoryItemRepository;

    // ExpenseCategory operations
    public List<ExpenseCategoryDto> getActiveCategories(User user) {
        Long familyId = user.getFamilies().getId();
        return expenseCategoryRepository.findAllByUser_Families_IdAndDeletedFalse(familyId)
                .stream()
                .map(category -> new ExpenseCategoryDto(
                        category.getId(),
                        category.getName(),
                        category.getIcon(),
                        category.getColor(),
                        category.getBudgetAmount()
                ))
                .toList();
    }

    public ExpenseCategoryDto createCategory(User user, CreateCategoryRequest request) {
        Long familyId = user.getFamilies().getId();
        if (expenseCategoryRepository.existsByUser_Families_IdAndNameIgnoreCaseAndDeletedFalse(familyId, request.name())) {
            throw new IllegalArgumentException("A category with this name already exists");
        }

        Category category = new Category();
        category.setUser(user);
        category.setName(request.name());
        category.setIcon(request.icon());
        category.setColor(request.color());
        category.setBudgetAmount(request.budgetAmount() != null ? request.budgetAmount() : BigDecimal.ZERO);
        category.setDeleted(false);

        Category saved = expenseCategoryRepository.save(category);
        return new ExpenseCategoryDto(
                saved.getId(),
                saved.getName(),
                saved.getIcon(),
                saved.getColor(),
                saved.getBudgetAmount()
        );
    }

    public ExpenseCategoryDto updateCategory(User user, Long id, UpdateCategoryRequest request) {
        Long familyId = user.getFamilies().getId();
        Category category = expenseCategoryRepository.findByIdAndUser_Families_Id(id, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (request.name() != null) {
            if (!request.name().equalsIgnoreCase(category.getName()) &&
                    expenseCategoryRepository.existsByUser_Families_IdAndNameIgnoreCaseAndDeletedFalse(familyId, request.name())) {
                throw new IllegalArgumentException("A category with this name already exists");
            }
            category.setName(request.name());
        }

        if (request.icon() != null) category.setIcon(request.icon());
        if (request.color() != null) category.setColor(request.color());
        if (request.budgetAmount() != null) category.setBudgetAmount(request.budgetAmount());

        Category saved = expenseCategoryRepository.save(category);
        return new ExpenseCategoryDto(
                saved.getId(),
                saved.getName(),
                saved.getIcon(),
                saved.getColor(),
                saved.getBudgetAmount()
        );
    }

    public void deleteCategory(User user, Long id) {
        Long familyId = user.getFamilies().getId();
        Category category = expenseCategoryRepository.findByIdAndUser_Families_Id(id, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setDeleted(true);
        expenseCategoryRepository.save(category);
    }

    // CategoryItem operations
    public List<CategoryItemDto> getCategoryItems(User user, Long categoryId) {
        Long familyId = user.getFamilies().getId();
        Category category = expenseCategoryRepository.findByIdAndUser_Families_Id(categoryId, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        return categoryItemRepository.findByExpenseCategoryAndDeletedFalseOrderByNameAsc(category)
                .stream()
                .map(item -> new CategoryItemDto(item.getId(), item.getName()))
                .toList();
    }

    public CategoryItemDto createCategoryItem(User user, Long categoryId, CreateCategoryItemRequest request) {
        Long familyId = user.getFamilies().getId();
        Category category = expenseCategoryRepository.findByIdAndUser_Families_Id(categoryId, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        CategoryItem item = new CategoryItem();
        item.setExpenseCategory(category);
        item.setName(request.name());
        item.setDeleted(false);

        CategoryItem saved = categoryItemRepository.save(item);
        return new CategoryItemDto(saved.getId(), saved.getName());
    }

    public CategoryItemDto updateCategoryItem(User user, Long categoryId, Long itemId, UpdateCategoryItemRequest request) {
        Long familyId = user.getFamilies().getId();
        Category category = expenseCategoryRepository.findByIdAndUser_Families_Id(categoryId, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        CategoryItem item = categoryItemRepository.findById(itemId)
                .filter(i -> i.getExpenseCategory().equals(category) && !i.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Category item not found"));

        item.setName(request.name());
        CategoryItem saved = categoryItemRepository.save(item);
        return new CategoryItemDto(saved.getId(), saved.getName());
    }

    public void deleteCategoryItem(User user, Long categoryId, Long itemId) {
        Long familyId = user.getFamilies().getId();
        Category category = expenseCategoryRepository.findByIdAndUser_Families_Id(categoryId, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        CategoryItem item = categoryItemRepository.findById(itemId)
                .filter(i -> i.getExpenseCategory().equals(category) && !i.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Category item not found"));

        item.setDeleted(true);
        categoryItemRepository.save(item);
    }
}