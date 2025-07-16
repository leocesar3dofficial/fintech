
package com.leo.fintech.account;

import java.util.List;
import java.util.Optional;
// ...existing code...
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private com.leo.fintech.auth.UserRepository userRepository;


    public List<AccountDto> getAccountsByUser(String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        return accountRepository.findAllByUserId(uuid).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public Optional<AccountDto> getAccountByIdAndUser(Long id, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        return accountRepository.findByIdAndUserId(id, uuid).map(this::toDto);
    }


    public AccountDto createAccountForUser(AccountDto dto, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
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




    public Optional<AccountDto> updateAccountByUser(Long id, AccountDto dto, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        return accountRepository.findByIdAndUserId(id, uuid).map(account -> {
            account.setName(dto.getName());
            account.setType(dto.getType());
            account.setInstitution(dto.getInstitution());
            return toDto(accountRepository.save(account));
        });
    }

    public boolean deleteAccountByUser(Long id, String userId) {
        java.util.UUID uuid = java.util.UUID.fromString(userId);
        Optional<Account> accountOpt = accountRepository.findByIdAndUserId(id, uuid);
        if (accountOpt.isPresent()) {
            accountRepository.deleteByIdAndUserId(id, uuid);
            return true;
        }
        return false;
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .institution(account.getInstitution())
                .build();
    }
}
