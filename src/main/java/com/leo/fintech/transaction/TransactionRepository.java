package com.leo.fintech.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccountId(Long accountId, UUID userId);

    Optional<Transaction> findByIdAndAccountId(Long id, Long accountId);

    void deleteByIdAndAccountId(Long id, Long accountId);

    List<Transaction> findAllByUserId(UUID userId);

    Optional<Transaction> findByIdAndUserId(Long id, UUID userId);
}
