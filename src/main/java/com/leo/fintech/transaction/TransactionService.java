package com.leo.fintech.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.leo.fintech.account.Account;
import com.leo.fintech.account.AccountRepository;
import com.leo.fintech.auth.SecurityUtils;
import com.leo.fintech.auth.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
    }

    public TransactionDto createTransactionForUser(TransactionDto dto) {
        UUID userId = SecurityUtils.extractUserId();

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Transaction transaction = transactionMapper.toEntity(dto);
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        transaction.setAccount(account);
        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toDto(saved);
    }

    public List<TransactionDto> getTransactionsByUser() {
        UUID userId = SecurityUtils.extractUserId();

        return transactionRepository.findAllByUserId(userId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getTransactionsByAccount(Long accountId) {
        UUID userId = SecurityUtils.extractUserId();

        return transactionRepository.findAllByAccountId(accountId, userId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<TransactionDto> getTransactionByIdAndUser(Long id) {
        UUID userId = SecurityUtils.extractUserId();

        return transactionRepository.findByIdAndUserId(id, userId)
                .map(transactionMapper::toDto);
    }

    @Transactional
    public TransactionDto updateTransaction(Long id, TransactionDto dto) {
        UUID userId = SecurityUtils.extractUserId();

        // Find the existing transaction and verify user ownership
        Transaction existingTransaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found or access denied"));

        // Handle account update if a account ID is provided
        if (dto.getAccountId() != null) {
            // First, validate that the account exists and belongs to the user
            Account account = accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found or access denied"));

            // Only update if the account is actually different
            if (!dto.getAccountId().equals(existingTransaction.getAccount().getId())) {
                existingTransaction.setAccount(account);
            }
        }

        // Update other fields from DTO
        transactionMapper.updateEntityFromDto(dto, existingTransaction);

        // Save and return
        Transaction updated = transactionRepository.save(existingTransaction);
        return transactionMapper.toDto(updated);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found or access denied"));
        transactionRepository.delete(transaction);
    }

    @Transactional
    public boolean deleteTransactionIfExists(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Optional<Transaction> transaction = transactionRepository.findByIdAndUserId(id, userId);

        if (transaction.isPresent()) {
            transactionRepository.delete(transaction.get());
            return true;
        }

        return false;
    }

}
