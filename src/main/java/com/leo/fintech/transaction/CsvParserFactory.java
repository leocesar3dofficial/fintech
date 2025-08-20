package com.leo.fintech.transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Component
public class CsvParserFactory {

    public <T extends CsvTransactionRecord> List<T> parseCsvFile(MultipartFile file, Class<T> recordClass,
            BankType bankType) throws IOException {
        Charset charset = determineCharset(bankType);

        try (Reader reader = new InputStreamReader(file.getInputStream(), charset)) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(recordClass)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            return csvToBean.parse();
        }
    }

    private Charset determineCharset(BankType bankType) {
        switch (bankType) {
            case BANCO_DO_BRASIL:
                return StandardCharsets.ISO_8859_1;
            case BANK_OF_AMERICA:
            default:
                return StandardCharsets.UTF_8;
        }
    }
}
