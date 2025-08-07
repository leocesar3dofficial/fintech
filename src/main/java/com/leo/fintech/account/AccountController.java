package com.leo.fintech.account;

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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<AccountDto> getAllAccounts(@AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
       
        return accountService.getUserAccounts(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        Optional<AccountDto> account = accountService.getUserAccountById(id, userId);
       
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto dto, 
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        AccountDto created = accountService.createUserAccount(dto, userId);
       
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long id, 
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        boolean deleted = accountService.deleteUserAccount(id, userId);
       
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable("id") Long id, @Valid @RequestBody AccountDto dto, 
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        Optional<AccountDto> updated = accountService.updateUserAccount(id, dto, userId);
      
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
