package com.leo.fintech.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final TransactionMapper transactionMapper;

    @Autowired
    private CsvBankDetector bankDetector;

    @Autowired
    private CsvParserFactory csvParserFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

    public List<TransactionDto> importTransactionsFromCsv(MultipartFile file, UUID userId) {
        try {
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            Account defaultAccount = accountRepository.findFirstByUserIdOrderByIdAsc(userId)
                    .orElseThrow(() -> new IllegalStateException("No account found for user"));

            Category defaultCategory = categoryRepository.findFirstByUserIdOrderByIdAsc(userId)
                    .orElseThrow(() -> new IllegalStateException("No category found for user"));

            BankType bankType = bankDetector.detectBankTypeFromFile(file);
            List<? extends CsvTransactionRecord> csvRecords = parseCsvByBankType(file, bankType);
            List<TransactionDto> savedTransactions = new ArrayList<>();

            for (CsvTransactionRecord record : csvRecords) {
                try {
                    TransactionDto dto = record.toTransactionDto(defaultAccount.getId(), defaultCategory.getId());
                    if (dto != null) {
                        TransactionDto savedTransaction = createTransactionForUser(dto, userId);
                        savedTransactions.add(savedTransaction);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing CSV record: " + e.getMessage());
                }
            }

            return savedTransactions;

        } catch (Exception e) {
            throw new RuntimeException("Error importing CSV file: " + e.getMessage(), e);
        }
    }

    private List<? extends CsvTransactionRecord> parseCsvByBankType(MultipartFile file, BankType bankType)
            throws IOException {
        switch (bankType) {
            case BANCO_DO_BRASIL:
                return csvParserFactory.parseCsvFile(file, BancoDoBrasilCsvRecord.class, bankType);
            case BANK_OF_AMERICA:
                return csvParserFactory.parseCsvFile(file, BankOfAmericaCsvRecord.class, bankType);
            default:
                throw new IllegalArgumentException("Unsupported bank type: " + bankType);
        }
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
