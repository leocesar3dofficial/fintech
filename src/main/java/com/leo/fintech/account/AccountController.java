package com.leo.fintech.account;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leo.fintech.auth.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;


    @GetMapping
    public List<AccountDto> getAllAccounts(Principal principal) {
        String userId = SecurityUtils.extractUserId();
        return accountService.getAccountsByUser(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable("id") Long id, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        Optional<AccountDto> account = accountService.getAccountByIdAndUser(id, userId);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto dto, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        AccountDto created = accountService.createAccountForUser(dto, userId);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long id, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        boolean deleted = accountService.deleteAccountByUser(id, userId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable("id") Long id, @Valid @RequestBody AccountDto dto, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        Optional<AccountDto> updated = accountService.updateAccountByUser(id, dto, userId);
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
