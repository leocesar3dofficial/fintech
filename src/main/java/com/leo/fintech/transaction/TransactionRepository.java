package com.leo.fintech.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccount_User_Id(UUID userId);

    Optional<Transaction> findByIdAndAccount_User_Id(Long id, UUID userId);

    List<Transaction> findAllByAccount_IdAndAccount_User_Id(Long accountId, UUID userId);

    List<Transaction> findAllByAccount_User_IdAndDateBetween(
            UUID userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findAllByAccount_IdAndAccount_User_IdAndDateBetween(
            Long accountId, UUID userId, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.account.user.id = :userId")
    void deleteAllByUserId(UUID userId);

}