
package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leo.fintech.auth.UserRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;

    public AccountService(
        AccountRepository accountRepository,
        AccountMapper accountMapper,                      
        UserRepository userRepository
        ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    public List<AccountDto> getAccountsByUser(String userId) {
        UUID uuid = UUID.fromString(userId);
        return accountRepository.findAllByUserId(uuid).stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccountByIdAndUser(Long id, String userId) {
        UUID uuid = UUID.fromString(userId);
        return accountRepository.findByIdAndUserId(id, uuid)
                .map(accountMapper::toDto);
    }

    public AccountDto createAccountForUser(AccountDto dto, String userId) {
        UUID uuid = UUID.fromString(userId);
        final com.leo.fintech.auth.User userEntity = userRepository.findById(uuid)
            .orElseThrow(() -> new IllegalStateException("User not found for id: " + userId));
        Account account = accountMapper.toEntity(dto);
        account.setUser(userEntity);
        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public Optional<AccountDto> updateAccountByUser(Long id, AccountDto dto, String userId) {
        UUID uuid = UUID.fromString(userId);
        return accountRepository.findByIdAndUserId(id, uuid).map(account -> {
            account.setName(dto.getName());
            account.setType(dto.getType());
            account.setInstitution(dto.getInstitution());
            return accountMapper.toDto(accountRepository.save(account));
        });
    }

    @Transactional
    public boolean deleteAccountByUser(Long id, String userId) {
        UUID uuid = UUID.fromString(userId);
        Optional<Account> account = accountRepository.findByIdAndUserId(id, uuid);
        if (account.isPresent()) {
            accountRepository.deleteByIdAndUserId(id, uuid);
            return true;
        }
        return false;
    }
}
