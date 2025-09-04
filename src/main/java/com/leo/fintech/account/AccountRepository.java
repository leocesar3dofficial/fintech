package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserId(UUID userId);

    Optional<Account> findByIdAndUserId(Long id, UUID userId);

    Optional<Account> findFirstByUserIdOrderByIdAsc(UUID userId);

    void deleteByIdAndUserId(Long id, UUID userId);

    @Modifying
    @Query("DELETE FROM Account a WHERE a.user.id = :userId")
    void deleteAllByUserId(UUID userId);
}
