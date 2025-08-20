package com.leo.fintech.transaction;

public interface CsvTransactionRecord {
    TransactionDto toTransactionDto(Long accountId, Long categoryId);
}
