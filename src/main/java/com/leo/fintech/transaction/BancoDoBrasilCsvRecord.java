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
public class BancoDoBrasilCsvRecord implements CsvTransactionRecord {
    @CsvBindByName(column = "Data")
    private String data;

    @CsvBindByName(column = "Histórico")
    private String historico;

    @CsvBindByName(column = "Valor")
    private String valor;

    @Override
    public TransactionDto toTransactionDto(Long accountId, Long categoryId) {
        try {
            if (data == null || valor == null || data.trim().isEmpty() || valor.trim().isEmpty()) {
                return null;
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate transactionDate = LocalDate.parse(data.trim(), dateFormatter);
            BigDecimal amount = new BigDecimal(valor.replace(",", "."));

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                amount = amount.negate();
            }

            return TransactionDto.builder()
                    .amount(amount)
                    .date(transactionDate)
                    .isRecurring(false)
                    .description(historico != null ? historico.trim() : "")
                    .accountId(accountId)
                    .categoryId(categoryId)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
