package com.leo.fintech.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankOfAmericaCsvRecord implements CsvTransactionRecord {
    @CsvBindByName(column = "Date")
    private String date;

    @CsvBindByName(column = "Description")
    private String description;

    @CsvBindByName(column = "Amount")
    private String amount;

    @Override
    public TransactionDto toTransactionDto(Long accountId, Long categoryId) {
        try {
            if (date == null || amount == null || date.trim().isEmpty() || amount.trim().isEmpty()) {
                return null;
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate transactionDate = LocalDate.parse(date.trim(), dateFormatter);
            BigDecimal transactionAmount = new BigDecimal(amount.replace(",", "").replace("$", ""));

            if (transactionAmount.compareTo(BigDecimal.ZERO) < 0) {
                transactionAmount = transactionAmount.negate();
            }

            return TransactionDto.builder()
                    .amount(transactionAmount)
                    .date(transactionDate)
                    .isRecurring(false)
                    .description(description != null ? description.trim() : "")
                    .accountId(accountId)
                    .categoryId(categoryId)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
