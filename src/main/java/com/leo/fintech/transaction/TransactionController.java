package com.leo.fintech.transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.leo.fintech.auth.JwtUserPrincipal;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CsvBankDetector bankDetector;

    private LocalDate parseYearMonth(String yearMonth, boolean startOfMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth);
            return startOfMonth ? ym.atDay(1) : ym.atEndOfMonth();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: YYYY-MM (e.g., 2024-01)");
        }
    }

    @GetMapping
    public List<TransactionDto> getAllTransactions(
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal,
            @RequestParam(value = "start_date", required = false) String startDate,
            @RequestParam(value = "end_date", required = false) String endDate) {

        UUID userId = UUID.fromString(userPrincipal.getUserId());

        if (startDate != null && endDate != null) {
            LocalDate start = parseYearMonth(startDate, true);
            LocalDate end = parseYearMonth(endDate, false);
            return transactionService.getUserTransactionsByDateRange(userId, start, end);
        }

        return transactionService.getUserTransactions(userId);
    }

    @GetMapping("/account/{id}")
    public List<TransactionDto> getAllTransactionsByAccount(
            @PathVariable("id") Long accountId,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal,
            @RequestParam(value = "start_date", required = false) String startDate,
            @RequestParam(value = "end_date", required = false) String endDate) {

        UUID userId = UUID.fromString(userPrincipal.getUserId());

        if (startDate != null && endDate != null) {
            LocalDate start = parseYearMonth(startDate, true);
            LocalDate end = parseYearMonth(endDate, false);
            return transactionService.getAccountTransactionsByDateRange(accountId, userId, start, end);
        }

        return transactionService.getAccountTransactions(accountId, userId);
    }

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto dto,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        TransactionDto created = transactionService.createTransactionForUser(dto, userId);

        return ResponseEntity.ok(created);
    }

    @PostMapping("/csv/import")
    public ResponseEntity<?> importTransactionsFromCsv(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Please select a CSV file to upload"));
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Please upload a valid CSV file"));
            }

            BankType detectedBank;

            try {
                detectedBank = bankDetector.detectBankTypeFromFile(file);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Unsupported CSV format: " + e.getMessage()));
            }

            UUID userId = UUID.fromString(userPrincipal.getUserId());
            List<TransactionDto> importedTransactions = transactionService.importTransactionsFromCsv(file, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "CSV imported successfully",
                    "bankType", detectedBank.getDisplayName(),
                    "importedCount", importedTransactions.size(),
                    "transactions", importedTransactions));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to import CSV: " + e.getMessage()));
        }
    }

    @GetMapping("/csv/supported-banks")
    public ResponseEntity<?> getSupportedBanks() {
        Map<String, String> supportedBanks = Arrays.stream(BankType.values())
                .collect(Collectors.toMap(
                        BankType::name,
                        BankType::getDisplayName));

        return ResponseEntity.ok(Map.of("supportedBanks", supportedBanks));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        Optional<TransactionDto> transaction = transactionService.getUserTransactionById(id, userId);

        return transaction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable("id") Long id,
            @Valid @RequestBody TransactionDto dto, @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        try {
            UUID userId = UUID.fromString(userPrincipal.getUserId());
            TransactionDto updated = transactionService.updateTransaction(id, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        try {
            UUID userId = UUID.fromString(userPrincipal.getUserId());
            transactionService.deleteTransaction(id, userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<Void> deleteTransactionSoft(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        boolean deleted = transactionService.deleteTransactionIfExists(id, userId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
