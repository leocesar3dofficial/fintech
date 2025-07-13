package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leo.fintech.auth.SecurityUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class AccountService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccountById(Long id) {
        return accountRepository.findById(id).map(this::toDto);
    }

    public AccountDto createAccount(AccountDto dto) {
        Account account = Account.builder()
                .name(dto.getName())
                .type(dto.getType())
                .institution(dto.getInstitution())
                .user(SecurityUtils.getCurrentUser())
                .build();

        Account saved = accountRepository.save(account);
        return toDto(saved);
    }   

    public Optional<AccountDto> updateAccount(Long id, AccountDto dto) {
        return accountRepository.findById(id).map(account -> {
            account.setName(dto.getName());
            account.setType(dto.getType());
            account.setInstitution(dto.getInstitution());
            return toDto(accountRepository.save(account));
        });
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .name(account.getName())
                .type(account.getType())
                .institution(account.getInstitution())
                .build();
    }
}
