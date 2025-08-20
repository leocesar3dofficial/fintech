package com.leo.fintech.transaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    @GetMapping
    public List<TransactionDto> getAllTransactions(@AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());

        return transactionService.getUserTransactions(userId);
    }

    @GetMapping("/account/{id}")
    public List<TransactionDto> getAllTransactionsByAccount(@PathVariable("id") Long accountId,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());

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

            UUID userId = UUID.fromString(userPrincipal.getUserId());
            List<TransactionDto> importedTransactions = transactionService.importTransactionsFromCsv(file, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "CSV imported successfully",
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
