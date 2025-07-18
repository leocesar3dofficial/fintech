package com.leo.fintech.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccountId(Long accountId);

    Optional<Transaction> findByIdAndAccountId(Long id, Long accountId);

    void deleteByIdAndAccountId(Long id, Long accountId);
}
