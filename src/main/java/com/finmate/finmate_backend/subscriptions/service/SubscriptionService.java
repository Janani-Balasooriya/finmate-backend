package com.finmate.finmate_backend.subscriptions.service;

import com.finmate.finmate_backend.accounts.model.Account;
import com.finmate.finmate_backend.accounts.repository.AccountRepository;
import com.finmate.finmate_backend.expenses.category.model.Category;
import com.finmate.finmate_backend.expenses.category.model.CategoryItem;
import com.finmate.finmate_backend.expenses.category.repository.CategoryItemRepository;
import com.finmate.finmate_backend.expenses.category.repository.CategoryRepository;
import com.finmate.finmate_backend.insights.service.InsightsCacheInvalidationService;
import com.finmate.finmate_backend.subscriptions.dto.SubscriptionDtos.*;
import com.finmate.finmate_backend.subscriptions.model.Subscription;
import com.finmate.finmate_backend.subscriptions.model.enums.BillingCycle;
import com.finmate.finmate_backend.subscriptions.repository.SubscriptionRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryItemRepository categoryItemRepository;
    private final AccountRepository accountRepository;
    private final InsightsCacheInvalidationService insightsCacheInvalidationService;

    private void validateUser(User user) {
        if (user == null) throw new IllegalArgumentException("User is null");
        if (user.getFamilies() == null) throw new IllegalArgumentException("User family is null");
    }

    public List<SubscriptionItemDto> getSubscriptions(User user) {
        validateUser(user);
        Long familyId = user.getFamilies().getId();

        return subscriptionRepository.findAllByUser_Families_Id(familyId)
                .stream()
                .map(subscription -> new SubscriptionItemDto(
                        subscription.getId(),
                        subscription.getName(),
                        subscription.getAmount(),
                        subscription.getAccount().getCurrencyCode().getCode(),
                        subscription.getBillingCycle().name().toLowerCase(),
                        subscription.getAccount().getId(),
                        subscription.getAccount().getName(),
                        subscription.getCategoryItem().getExpenseCategory().getId(),
                        subscription.getCategoryItem().getExpenseCategory().getName(),
                        subscription.getCategoryItem().getExpenseCategory().getColor(),
                        subscription.getCategoryItem().getId(),
                        subscription.getCategoryItem().getName(),
                        subscription.getNextChargeDate(),
                        subscription.isActive()
                ))
                .toList();
    }

    public SubscriptionItemDto getSubscription(User user, Long id) {
        validateUser(user);
        Long familyId = user.getFamilies().getId();

        Subscription subscription = subscriptionRepository.findByIdAndUser_Families_Id(id, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        return toDto(subscription);
    }

    @Transactional
    public SubscriptionItemDto createWithCategoryItem(User user, CreateSubscriptionRequest request) {
        validateUser(user);
        Long familyId = user.getFamilies().getId();

        Category category = categoryRepository.findByIdAndUser_Families_Id(request.categoryId(), familyId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (categoryItemRepository.existsByExpenseCategory_IdAndExpenseCategory_User_Families_IdAndNameIgnoreCase(
                request.categoryId(), familyId, request.serviceName())) {
            throw new IllegalArgumentException("Category item already exists");
        }

        Account account = accountRepository.findByIdAndUser(request.accountId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        CategoryItem categoryItem = new CategoryItem();
        categoryItem.setExpenseCategory(category);
        categoryItem.setName(request.serviceName());
        categoryItem.setDeleted(false);
        categoryItem = categoryItemRepository.save(categoryItem);

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setName(request.serviceName());
        subscription.setAmount(request.amount());
        subscription.setBillingCycle(BillingCycle.valueOf(request.billingCycle().toLowerCase()));
        subscription.setNextChargeDate(request.nextBilling());
        subscription.setActive(true);
        subscription.setAccount(account);
        subscription.setCategoryItem(categoryItem);

        Subscription saved = subscriptionRepository.save(subscription);
        insightsCacheInvalidationService.invalidateInsightsCache(familyId);
        return toDto(saved);
    }

    public SubscriptionItemDto updateSubscription(User user, Long id, UpdateSubscriptionRequest request) {
        validateUser(user);
        Long familyId = user.getFamilies().getId();

        Subscription subscription = subscriptionRepository.findByIdAndUser_Families_Id(id, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        if (request.name() != null) subscription.setName(request.name());
        if (request.amount() != null) subscription.setAmount(request.amount());
        if (request.billingCycle() != null)
            subscription.setBillingCycle(BillingCycle.valueOf(request.billingCycle().toLowerCase()));

        if (request.accountId() != null) {
            Account account = accountRepository.findByIdAndUser(request.accountId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            subscription.setAccount(account);
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUser_Families_Id(request.categoryId(), familyId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            subscription.getCategoryItem().setExpenseCategory(category);
            categoryItemRepository.save(subscription.getCategoryItem());
        }

        if (request.nextBilling() != null) subscription.setNextChargeDate(request.nextBilling());
        if (request.isActive() != null) subscription.setActive(request.isActive());

        Subscription saved = subscriptionRepository.save(subscription);
        insightsCacheInvalidationService.invalidateInsightsCache(familyId);
        return toDto(saved);
    }

    public void deleteSubscription(User user, Long id) {
        validateUser(user);
        Long familyId = user.getFamilies().getId();

        Subscription subscription = subscriptionRepository.findByIdAndUser_Families_Id(id, familyId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        subscriptionRepository.delete(subscription);
        insightsCacheInvalidationService.invalidateInsightsCache(familyId);
    }

    private SubscriptionItemDto toDto(Subscription s) {
        return new SubscriptionItemDto(
                s.getId(),
                s.getName(),
                s.getAmount(),
                s.getAccount().getCurrencyCode().getCode(),
                s.getBillingCycle().name().toLowerCase(),
                s.getAccount().getId(),
                s.getAccount().getName(),
                s.getCategoryItem().getExpenseCategory().getId(),
                s.getCategoryItem().getExpenseCategory().getName(),
                s.getCategoryItem().getExpenseCategory().getColor(),
                s.getCategoryItem().getId(),
                s.getCategoryItem().getName(),
                s.getNextChargeDate(),
                s.isActive()
        );
    }
}
