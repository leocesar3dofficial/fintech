
package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leo.fintech.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;

    public List<AccountDto> getUserAccounts(UUID userId) {
        return accountRepository.findAllByUserId(userId).stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getUserAccountById(Long id, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .map(accountMapper::toDto);
    }

    public AccountDto createUserAccount(AccountDto dto, UUID userId) {
        final com.leo.fintech.user.User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Account account = accountMapper.toEntity(dto);
        account.setUser(userEntity);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public Optional<AccountDto> updateUserAccount(Long id, AccountDto dto, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId).map(account -> {
            account.setName(dto.getName());
            account.setType(dto.getType());
            account.setInstitution(dto.getInstitution());
            return accountMapper.toDto(accountRepository.save(account));
        });
    }

    @Transactional
    public void deleteUserAccount(Long id, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found or access denied"));

        accountRepository.delete(account);
    }

    @Transactional
    public boolean deleteAccountIfExists(Long id, UUID userId) {
        Optional<Account> account = accountRepository.findByIdAndUserId(id, userId);

        if (account.isPresent()) {
            accountRepository.delete(account.get());
            return true;
        }

        return false;
    }
}
