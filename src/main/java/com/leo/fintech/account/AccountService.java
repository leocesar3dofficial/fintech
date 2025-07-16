package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leo.fintech.auth.SecurityUtils;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private com.leo.fintech.auth.UserRepository userRepository;

    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccountById(Long id) {
        return accountRepository.findById(id).map(this::toDto);
    }

    public AccountDto createAccount(AccountDto dto) {
        Object principal = SecurityUtils.getCurrentUserPrincipal();
        final String userId;
        if (principal instanceof com.leo.fintech.auth.JwtUserPrincipal jwtPrincipal) {
            userId = jwtPrincipal.getUserId();
        } else if (principal instanceof com.leo.fintech.auth.User user) {
            userId = user.getId().toString();
        } else {
            throw new IllegalStateException("Unknown principal type for account creation");
        }
        final java.util.UUID uuid = java.util.UUID.fromString(userId);
        final com.leo.fintech.auth.User userEntity = userRepository.findById(uuid)
            .orElseThrow(() -> new IllegalStateException("User not found for id: " + userId));
        Account account = Account.builder()
                .name(dto.getName())
                .type(dto.getType())
                .institution(dto.getInstitution())
                .user(userEntity)
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
