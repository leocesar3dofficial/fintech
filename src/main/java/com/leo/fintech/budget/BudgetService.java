package com.leo.fintech.budget;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leo.fintech.auth.User;
import com.leo.fintech.auth.UserRepository;
import com.leo.fintech.category.Category;
import com.leo.fintech.category.CategoryRepository;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(
        BudgetRepository budgetRepository,
        UserRepository userRepository,
        CategoryRepository categoryRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public BudgetDto createBudgetForUser(BudgetDto budgetDto, String userId) {
        UUID uuid = UUID.fromString(userId);
        Category category = categoryRepository.findById(budgetDto.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + budgetDto.getCategory().getId()));

        // 2. Check for duplicate budget (same user, category, and month)
        boolean budgetExists = budgetRepository.existsByUser_IdAndCategory_IdAndMonth(
            user.getId(), 
            category.getId(), 
            budgetDto.getMonth()
        );
        if (budgetExists) {
            throw new DuplicateResourceException(
                "A budget for this category already exists for the month " + budgetDto.getMonth()
            );
        }

        // 3. Map DTO to Entity and save
        Budget budget = toEntity(budgetDto, user, category);
        Budget savedBudget = budgetRepository.save(budget);

        // 4. Return the mapped DTO
        return toDto(savedBudget);
    }

    public BudgetDto getBudgetById(Long id, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        return toDto(budget);
    }

    public List<BudgetDto> getBudgetsByUserAndMonth(UUID userId, YearMonth month) {
        if (!userRepository.existsById(userId)) {
             throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        List<Budget> budgets = budgetRepository.findByUser_IdAndMonth(userId, month);
        return budgets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetDto updateBudget(Long id, BudgetDto budgetDto, UUID userId) {
        // Find the existing budget, ensuring it belongs to the user trying to update it
        Budget existingBudget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        // Security check: ensure the user owns this budget
        if (!existingBudget.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("User is not authorized to update this budget.");
        }
        
        // Update the amount (typically only the amount is updatable)
        existingBudget.setAmount(budgetDto.getAmount());
        
        Budget updatedBudget = budgetRepository.save(existingBudget);
        return toDto(updatedBudget);
    }

    @Transactional
    public void deleteBudget(Long id, UUID userId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        // Security check: ensure the user owns this budget
        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("User is not authorized to delete this budget.");
        }

        budgetRepository.delete(budget);
    }

}
