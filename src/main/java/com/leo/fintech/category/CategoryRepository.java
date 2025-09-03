package com.leo.fintech.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Category> findByIdAndUserId(Long id, UUID userId);

    Optional<Category> findFirstByUserIdOrderByIdAsc(UUID userId);
    
    void deleteByIdAndUserId(Long id, UUID userId);

    @Modifying
    void deleteAllByUserId(UUID userId);

    List<Category> findAllByUserIdAndIsIncome(UUID userId, Boolean isIncome);

    Page<Category> findAllByUserIdAndNameIgnoreCaseContaining(UUID userId, String name, Pageable pageable);
}
