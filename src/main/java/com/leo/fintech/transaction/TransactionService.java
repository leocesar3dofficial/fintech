package com.leo.fintech.transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.leo.fintech.account.Account;
import com.leo.fintech.account.AccountRepository;
import com.leo.fintech.category.Category;
import com.leo.fintech.category.CategoryRepository;
import com.leo.fintech.user.UserRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

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

    public List<TransactionDto> importTransactionsFromCsv(MultipartFile file, UUID userId) {
        try {
            // Validate user exists
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            // Get default account and category
            Account defaultAccount = accountRepository.findFirstByUserIdOrderByIdAsc(userId)
                    .orElseThrow(() -> new IllegalStateException("No account found for user"));

            Category defaultCategory = categoryRepository.findFirstByUserIdOrderByIdAsc(userId)
                    .orElseThrow(() -> new IllegalStateException("No category found for user"));

            // Parse CSV
            List<CsvRecordBancoDoBrasil> csvRecords = parseCsvFile(file);

            // Convert to DTOs and save
            List<TransactionDto> savedTransactions = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            for (CsvRecordBancoDoBrasil record : csvRecords) {
                try {
                    // Skip records with invalid data
                    if (record.getData() == null || record.getValor() == null ||
                            record.getData().trim().isEmpty() || record.getValor().trim().isEmpty()) {
                        continue;
                    }

                    // Parse date
                    LocalDate transactionDate = LocalDate.parse(record.getData().trim(), dateFormatter);

                    // Parse amount and make it positive if negative
                    BigDecimal amount = new BigDecimal(record.getValor().replace(",", "."));
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        amount = amount.negate();
                    }

                    // Create DTO
                    TransactionDto dto = TransactionDto.builder()
                            .amount(amount)
                            .date(transactionDate)
                            .isRecurring(false)
                            .description(record.getHistorico() != null ? record.getHistorico().trim() : "")
                            .accountId(defaultAccount.getId())
                            .categoryId(defaultCategory.getId())
                            .build();

                    // Save transaction
                    TransactionDto savedTransaction = createTransactionForUser(dto, userId);
                    savedTransactions.add(savedTransaction);

                } catch (Exception e) {
                    // Log the error and continue with next record
                    System.err.println("Error processing CSV record: " + e.getMessage());
                    continue;
                }
            }

            return savedTransactions;

        } catch (Exception e) {
            throw new RuntimeException("Error importing CSV file: " + e.getMessage(), e);
        }
    }

    private List<CsvRecordBancoDoBrasil> parseCsvFile(MultipartFile file) throws IOException {
        // try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1)) {
            CsvToBean<CsvRecordBancoDoBrasil> csvToBean = new CsvToBeanBuilder<CsvRecordBancoDoBrasil>(reader)
                    .withType(CsvRecordBancoDoBrasil.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            return csvToBean.parse();
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
