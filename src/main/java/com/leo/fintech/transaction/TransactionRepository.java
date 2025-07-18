package com.leo.fintech.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findAllByUserId(UUID userId);
    
    Optional<Transaction> findByIdAndUserId(Long id, UUID userId);
    
    void deleteByIdAndUserId(Long id, UUID userId);
    
    List<Transaction> findAllByUserIdAndCategoryId(UUID userId, Long categoryId);
    
    List<Transaction> findAllByUserIdAndAccountId(UUID userId, Long accountId);
    
}
