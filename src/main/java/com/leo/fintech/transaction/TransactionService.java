package com.leo.fintech.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.leo.fintech.account.Account;
import com.leo.fintech.account.AccountRepository;
import com.leo.fintech.category.Category;
import com.leo.fintech.category.CategoryRepository;
import com.leo.fintech.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public TransactionDto createTransactionForUser(TransactionDto dto, UUID userId) {
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

    public List<TransactionDto> getUserTransactions(UUID userId) {
        return transactionRepository.findAllByAccount_User_Id(userId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDto> getAccountTransactions(Long accountId, UUID userId) {
        return transactionRepository.findAllByAccount_IdAndAccount_User_Id(accountId, userId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<TransactionDto> getUserTransactionById(Long id, UUID userId) {
        return transactionRepository.findByIdAndAccount_User_Id(id, userId)
                .map(transactionMapper::toDto);
    }

    @Transactional
    public TransactionDto updateTransaction(Long id, TransactionDto dto, UUID userId) {
        Transaction existingTransaction = transactionRepository.findByIdAndAccount_User_Id(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found or access denied"));

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found or access denied"));

            if (!dto.getAccountId().equals(existingTransaction.getAccount().getId())) {
                existingTransaction.setAccount(account);
            }
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found or access denied"));

            if (!dto.getCategoryId().equals(existingTransaction.getCategory().getId())) {
                existingTransaction.setCategory(category);
            }
        }

        transactionMapper.updateEntityFromDto(dto, existingTransaction);
        Transaction updated = transactionRepository.save(existingTransaction);

        return transactionMapper.toDto(updated);
    }

    @Transactional
    public void deleteTransaction(Long id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndAccount_User_Id(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found or access denied"));
        transactionRepository.delete(transaction);
    }

    @Transactional
    public boolean deleteTransactionIfExists(Long id, UUID userId) {
        Optional<Transaction> transaction = transactionRepository.findByIdAndAccount_User_Id(id, userId);

        if (transaction.isPresent()) {
            transactionRepository.delete(transaction.get());
            return true;
        }

        return false;
    }

}
