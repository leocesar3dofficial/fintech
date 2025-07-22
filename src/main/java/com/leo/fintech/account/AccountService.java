
package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leo.fintech.auth.SecurityUtils;
import com.leo.fintech.auth.UserRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;

    public AccountService(
            AccountRepository accountRepository,
            AccountMapper accountMapper,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    public List<AccountDto> getAccountsByUser() {
        UUID userId = SecurityUtils.extractUserId();

        return accountRepository.findAllByUserId(userId).stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccountByIdAndUser(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        return accountRepository.findByIdAndUserId(id, userId)
                .map(accountMapper::toDto);
    }

    public AccountDto createAccountForUser(AccountDto dto) {
        UUID userId = SecurityUtils.extractUserId();
        final com.leo.fintech.auth.User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Account account = accountMapper.toEntity(dto);
        account.setUser(userEntity);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public Optional<AccountDto> updateAccountByUser(Long id, AccountDto dto) {
        UUID userId = SecurityUtils.extractUserId();

        return accountRepository.findByIdAndUserId(id, userId).map(account -> {
            account.setName(dto.getName());
            account.setType(dto.getType());
            account.setInstitution(dto.getInstitution());
            return accountMapper.toDto(accountRepository.save(account));
        });
    }

    @Transactional
    public boolean deleteAccountByUser(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Optional<Account> account = accountRepository.findByIdAndUserId(id, userId);

        if (account.isPresent()) {
            accountRepository.deleteByIdAndUserId(id, userId);
            return true;
        }
        return false;
    }
}
