package com.leo.fintech.goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leo.fintech.auth.JwtUserPrincipal;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @GetMapping
    public List<GoalDto> getAllGoals(@AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());

        return goalService.getUserGoals(userId);
    }

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(@Valid @RequestBody GoalDto dto,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        GoalDto created = goalService.createUserGoal(dto, userId);

        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDto> getGoalById(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        Optional<GoalDto> goal = goalService.getGoalByIdAndUser(id, userId);

        return goal.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> updateGoal(
            @PathVariable("id") Long id,
            @Valid @RequestBody GoalDto dto, @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        try {
            UUID userId = UUID.fromString(userPrincipal.getUserId());
            GoalDto updated = goalService.updateGoal(id, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        try {
            UUID userId = UUID.fromString(userPrincipal.getUserId());
            goalService.deleteGoal(id, userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<Void> deleteGoalSoft(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        boolean deleted = goalService.deleteGoalIfExists(id, userId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
