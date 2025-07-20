package com.leo.fintech.budget;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.leo.fintech.auth.SecurityUtils;
import com.leo.fintech.auth.User;
import com.leo.fintech.auth.UserRepository;
import com.leo.fintech.category.Category;
import com.leo.fintech.category.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    public BudgetService(
        BudgetRepository budgetRepository,
        UserRepository userRepository,
        CategoryRepository categoryRepository,
        BudgetMapper budgetMapper
    ) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.budgetMapper = budgetMapper;
    }

    public BudgetDto createBudgetForUser(BudgetDto dto) {
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
}
