package com.leo.fintech.account;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUserId(UUID userId);
    Optional<Account> findByIdAndUserId(Long id, UUID userId);
    void deleteByIdAndUserId(Long id, UUID userId);
}
