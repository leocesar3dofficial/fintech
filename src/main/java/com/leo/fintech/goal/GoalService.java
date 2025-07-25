package com.leo.fintech.goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.leo.fintech.auth.SecurityUtils;
import com.leo.fintech.auth.User;
import com.leo.fintech.auth.UserRepository;
import com.leo.fintech.account.Account;
import com.leo.fintech.account.AccountRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final GoalMapper goalMapper;

    public GoalDto createUserGoal(GoalDto dto) {
        UUID userId = SecurityUtils.extractUserId();

        final User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Goal goal = goalMapper.toEntity(dto);
        goal.setUser(userEntity);
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        goal.setAccount(account);
        Goal saved = goalRepository.save(goal);

        return goalMapper.toDto(saved);
    }

    public List<GoalDto> getUserGoals() {
        UUID userId = SecurityUtils.extractUserId();

        return goalRepository.findAllByUserId(userId).stream()
                .map(goalMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<GoalDto> getGoalByIdAndUser(Long id) {
        UUID userId = SecurityUtils.extractUserId();

        return goalRepository.findByIdAndUserId(id, userId)
                .map(goalMapper::toDto);
    }

    @Transactional
    public GoalDto updateGoal(Long id, GoalDto dto) {
        UUID userId = SecurityUtils.extractUserId();
        Goal existingGoal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found or access denied"));

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found or access denied"));

            if (!dto.getAccountId().equals(existingGoal.getAccount().getId())) {
                existingGoal.setAccount(account);
            }
        }

        goalMapper.updateEntityFromDto(dto, existingGoal);
        Goal updated = goalRepository.save(existingGoal);

        return goalMapper.toDto(updated);
    }

    @Transactional
    public void deleteGoal(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found or access denied"));
        goalRepository.delete(goal);
    }

    @Transactional
    public boolean deleteGoalIfExists(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Optional<Goal> goal = goalRepository.findByIdAndUserId(id, userId);

        if (goal.isPresent()) {
            goalRepository.delete(goal.get());
            return true;
        }

        return false;
    }
}
