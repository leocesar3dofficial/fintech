package com.leo.fintech.budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leo.fintech.auth.User;
import com.leo.fintech.category.Category;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findAllByUserId(UUID userId);
    
    Optional<Budget> findByIdAndUserId(Long id, UUID userId);
    
    void deleteByIdAndUserId(Long id, UUID userId);
    
    List<Budget> findAllByUserIdAndCategoryId(UUID userId, Long categoryId);
    
    Optional<Budget> findByMonthAndCategoryAndUser(LocalDate month, Category category, User user);
}
