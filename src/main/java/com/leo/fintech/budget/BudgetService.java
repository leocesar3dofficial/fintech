package com.leo.fintech.budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.leo.fintech.auth.SecurityUtils;
import com.leo.fintech.auth.User;
import com.leo.fintech.auth.UserRepository;
import com.leo.fintech.category.Category;
import com.leo.fintech.category.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    @CacheEvict(value = "userBudgets", key = "T(com.leo.fintech.auth.SecurityUtils).extractUserId()")
    public BudgetDto createUserBudget(BudgetDto dto) {
        UUID userId = SecurityUtils.extractUserId();
        final User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Budget budget = budgetMapper.toEntity(dto);
        budget.setUser(userEntity);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        budget.setCategory(category);
        Budget saved = budgetRepository.save(budget);
        BudgetDto result = budgetMapper.toDto(saved);
        cacheIndividualBudget(result);

        return result;
    }

    @Cacheable(value = "userBudgets", key = "T(com.leo.fintech.auth.SecurityUtils).extractUserId()")
    public List<BudgetDto> getUserBudgets() {
        UUID userId = SecurityUtils.extractUserId();
        log.debug("Fetching budgets from database for user: {}", userId);
        return budgetRepository.findAllByUserId(userId).stream()
                .map(budgetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "individualBudget", key = "#id + '_' + T(com.leo.fintech.auth.SecurityUtils).extractUserId()", unless = "#result == null")
    public BudgetDto getUserBudgetById(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        log.debug("Fetching budget {} from database for user: {}", id, userId);
        return budgetRepository.findByIdAndUserId(id, userId)
                .map(budgetMapper::toDto)
                .orElse(null);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "individualBudget", key = "#id + '_' + T(com.leo.fintech.auth.SecurityUtils).extractUserId()"),
            @CacheEvict(value = "userBudgets", key = "T(com.leo.fintech.auth.SecurityUtils).extractUserId()")
    })
    public BudgetDto updateBudget(Long id, BudgetDto dto) {
        UUID userId = SecurityUtils.extractUserId();
        Budget existingBudget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found or access denied"));

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found or access denied"));
            if (!dto.getCategoryId().equals(existingBudget.getCategory().getId())) {
                existingBudget.setCategory(category);
            }
        }

        budgetMapper.updateEntityFromDto(dto, existingBudget);
        Budget updated = budgetRepository.save(existingBudget);
        BudgetDto result = budgetMapper.toDto(updated);
        cacheIndividualBudget(result);

        return result;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "individualBudget", key = "#id + '_' + T(com.leo.fintech.auth.SecurityUtils).extractUserId()"),
            @CacheEvict(value = "userBudgets", key = "T(com.leo.fintech.auth.SecurityUtils).extractUserId()")
    })
    public void deleteBudget(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found or access denied"));
        budgetRepository.delete(budget);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "individualBudget", key = "#id + '_' + T(com.leo.fintech.auth.SecurityUtils).extractUserId()"),
            @CacheEvict(value = "userBudgets", key = "T(com.leo.fintech.auth.SecurityUtils).extractUserId()")
    })
    public boolean deleteBudgetIfExists(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Optional<Budget> budget = budgetRepository.findByIdAndUserId(id, userId);
        if (budget.isPresent()) {
            budgetRepository.delete(budget.get());
            return true;
        }
        return false;
    }

    @CachePut(value = "individualBudget", key = "#budget.id + '_' + #budget.userId")
    public BudgetDto cacheIndividualBudget(BudgetDto budget) {
        return budget;
    }

    @CacheEvict(value = { "userBudgets", "individualBudget" }, allEntries = true)
    public void clearAllBudgetCaches() {
        log.info("Clearing all budget caches");
    }

    @CacheEvict(value = "userBudgets", key = "#userId")
    public void clearUserBudgetCache(UUID userId) {
        log.info("Clearing user budget list cache for user: {}", userId);
    }
}