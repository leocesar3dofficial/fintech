package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leo.fintech.auth.User;
import com.leo.fintech.auth.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class AccountService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccountById(Long id) {
        return accountRepository.findById(id).map(this::toDto);
    }

    public AccountDto createAccount(AccountDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Account account = Account.builder()
                .name(dto.getName())
                .type(dto.getType())
                .institution(dto.getInstitution())
                .user(user)
                .build();
        Account saved = accountRepository.save(account);
        accountRepository.flush();
        return toDto(saved);
    }

    public Optional<AccountDto> updateAccount(Long id, AccountDto dto) {
        return accountRepository.findById(id).map(account -> {
            account.setName(dto.getName());
            account.setType(dto.getType());
            account.setInstitution(dto.getInstitution());
            account.setBalance(dto.getBalance());
            account.setCreatedAt(dto.getCreatedAt());
            // User update omitted for simplicity
            return toDto(accountRepository.save(account));
        });
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .institution(account.getInstitution())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .userId(account.getUser().getId())
                .build();
    }
}
