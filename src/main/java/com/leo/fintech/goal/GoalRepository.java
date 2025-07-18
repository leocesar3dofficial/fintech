package com.leo.fintech.goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findAllByUserId(UUID userId);

    Optional<Goal> findByIdAndUserId(Long id, UUID userId);

    void deleteByIdAndUserId(Long id, UUID userId);
}
