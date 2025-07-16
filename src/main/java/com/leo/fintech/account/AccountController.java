package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.security.Principal;
// ...existing code...

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<AccountDto> getAllAccounts(Principal principal) {
        // Only return accounts belonging to the logged-in user
        return accountService.getAccountsByUser(principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id, Principal principal) {
        Optional<AccountDto> account = accountService.getAccountByIdAndUser(id, principal.getName());
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto dto, Principal principal) {
        // Create account for the logged-in user only
        AccountDto created = accountService.createAccountForUser(dto, principal.getName());
        return ResponseEntity.ok(created);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, Principal principal) {
        // Only allow deletion if the account belongs to the logged-in user
        boolean deleted = accountService.deleteAccountByUser(id, principal.getName());
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountDto dto, Principal principal) {
        Optional<AccountDto> updated = accountService.updateAccountByUser(id, dto, principal.getName());
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
