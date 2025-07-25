package com.leo.fintech.budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.leo.fintech.auth.SecurityUtils;
import com.leo.fintech.auth.User;
import com.leo.fintech.auth.UserRepository;
import com.leo.fintech.category.Category;
import com.leo.fintech.category.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

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

        return budgetMapper.toDto(saved);
    }

    public List<BudgetDto> getUserBudgets() {
        UUID userId = SecurityUtils.extractUserId();

        return budgetRepository.findAllByUserId(userId).stream()
                .map(budgetMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<BudgetDto> getUserBudgetById(Long id) {
        UUID userId = SecurityUtils.extractUserId();

        return budgetRepository.findByIdAndUserId(id, userId)
                .map(budgetMapper::toDto);
    }

    @Transactional
    public BudgetDto updateBudget(Long id, BudgetDto dto) {
        UUID userId = SecurityUtils.extractUserId();

        // Find the existing budget and verify user ownership
        Budget existingBudget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found or access denied"));

        // Handle category update if a category ID is provided
        if (dto.getCategoryId() != null) {
            // First, validate that the category exists and belongs to the user
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found or access denied"));

            // Only update if the category is actually different
            if (!dto.getCategoryId().equals(existingBudget.getCategory().getId())) {
                existingBudget.setCategory(category);
            }
        }

        // Update other fields from DTO
        budgetMapper.updateEntityFromDto(dto, existingBudget);

        // Save and return
        Budget updated = budgetRepository.save(existingBudget);
        return budgetMapper.toDto(updated);
    }

    @Transactional
    public void deleteBudget(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found or access denied"));
        budgetRepository.delete(budget);
    }

    @Transactional
    public boolean deleteBudgetIfExists(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Optional<Budget> budget = budgetRepository.findByIdAndUserId(id, userId);

        if (budget.isPresent()) {
            budgetRepository.delete(budget.get());
            return true;
        }

        return false;
    }
}
