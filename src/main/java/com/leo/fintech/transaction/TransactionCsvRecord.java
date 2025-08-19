package com.leo.fintech.transaction;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCsvRecord {
    @CsvBindByName(column = "Data")
    private String data;

    @CsvBindByName(column = "Histórico")
    private String historico;

    @CsvBindByName(column = "Valor")
    private String valor;
}
