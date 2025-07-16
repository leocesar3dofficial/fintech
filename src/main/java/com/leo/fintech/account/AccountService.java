
package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final com.leo.fintech.auth.UserRepository userRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository,
                          com.leo.fintech.auth.UserRepository userRepository,
                          AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    public List<AccountDto> getAccountsByUser(String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        return accountRepository.findAllByUserId(uuid).stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccountByIdAndUser(Long id, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        return accountRepository.findByIdAndUserId(id, uuid)
                .map(accountMapper::toDto);
    }

    public AccountDto createAccountForUser(AccountDto dto, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        final com.leo.fintech.auth.User userEntity = userRepository.findById(uuid)
            .orElseThrow(() -> new IllegalStateException("User not found for id: " + userId));
        Account account = accountMapper.toEntity(dto);
        account.setUser(userEntity);
        return accountMapper.toDto(accountRepository.save(account));
    }

    public Optional<AccountDto> updateAccountByUser(Long id, AccountDto dto, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        return accountRepository.findByIdAndUserId(id, uuid).map(account -> {
            account.setName(dto.getName());
            account.setType(dto.getType());
            account.setInstitution(dto.getInstitution());
            return accountMapper.toDto(accountRepository.save(account));
        });
    }

    public boolean deleteAccountByUser(Long id, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        return accountRepository.findByIdAndUserId(id, uuid)
                .map(_ -> {
                    accountRepository.deleteByIdAndUserId(id, uuid);
                    return true;
                }).orElse(false);
    }
}
