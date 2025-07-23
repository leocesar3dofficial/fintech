package com.leo.fintech.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccountId(Long accountId);
    
    Optional<Transaction> findByIdAndAccountId(Long id, Long accountId);
    
    void deleteByIdAndAccountId(Long id, Long accountId);

    List<Transaction> findAllByAccount_User_Id(UUID userId);
    
    Optional<Transaction> findByIdAndAccount_User_Id(Long id, UUID userId);
    
    List<Transaction> findAllByAccount_IdAndAccount_User_Id(Long accountId, UUID userId);
}