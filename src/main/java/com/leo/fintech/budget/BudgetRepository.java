package com.leo.fintech.budget;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leo.fintech.category.Category;
import com.leo.fintech.user.User;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByIdAndUserId(Long id, UUID userId);

    Optional<Budget> findByMonthAndCategoryAndUser(YearMonth month, Category category, User user);

    List<Budget> findAllByUserId(UUID userId);

    List<Budget> findAllByUserIdAndCategoryId(UUID userId, Long categoryId);

    List<Budget> findByUser_IdAndMonth(UUID userId, YearMonth month);

    void deleteByIdAndUserId(Long id, UUID userId);

    boolean existsByUser_IdAndCategory_IdAndMonth(UUID uuid, Long id, YearMonth month);

    Optional<Budget> findByUserIdAndCategoryIdAndMonth(UUID userId, Long categoryId, YearMonth month);
}
