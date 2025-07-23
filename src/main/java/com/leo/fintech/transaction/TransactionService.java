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
import com.leo.fintech.category.Category;
import com.leo.fintech.category.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
    }

    public TransactionDto createTransactionForUser(TransactionDto dto) {
        UUID userId = SecurityUtils.extractUserId();
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Account account = accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found or access denied"));
        Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found or access denied"));
        Transaction transaction = transactionMapper.toEntity(dto);
        transaction.setAccount(account);
        transaction.setCategory(category);
        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toDto(saved);
    }

    public List<TransactionDto> getTransactionsByUser() {
        UUID userId = SecurityUtils.extractUserId();

        return transactionRepository.findAllByAccount_User_Id(userId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getTransactionsByAccount(Long accountId) {
        UUID userId = SecurityUtils.extractUserId();

        return transactionRepository.findAllByAccount_IdAndAccount_User_Id(accountId, userId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<TransactionDto> getTransactionByIdAndUser(Long id) {
        UUID userId = SecurityUtils.extractUserId();

        return transactionRepository.findByIdAndAccount_User_Id(id, userId)
                .map(transactionMapper::toDto);
    }

    @Transactional
    public TransactionDto updateTransaction(Long id, TransactionDto dto) {
        UUID userId = SecurityUtils.extractUserId();
        Transaction existingTransaction = transactionRepository.findByIdAndAccount_User_Id(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found or access denied"));

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found or access denied"));

            if (!dto.getAccountId().equals(existingTransaction.getAccount().getId())) {
                existingTransaction.setAccount(account);
            }
        }

        transactionMapper.updateEntityFromDto(dto, existingTransaction);
        Transaction updated = transactionRepository.save(existingTransaction);

        return transactionMapper.toDto(updated);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Transaction transaction = transactionRepository.findByIdAndAccount_User_Id(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found or access denied"));
        transactionRepository.delete(transaction);
    }

    @Transactional
    public boolean deleteTransactionIfExists(Long id) {
        UUID userId = SecurityUtils.extractUserId();
        Optional<Transaction> transaction = transactionRepository.findByIdAndAccount_User_Id(id, userId);

        if (transaction.isPresent()) {
            transactionRepository.delete(transaction.get());
            return true;
        }

        return false;
    }

}
