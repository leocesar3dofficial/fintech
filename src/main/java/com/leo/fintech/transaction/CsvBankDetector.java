package com.leo.fintech.transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Component
public class CsvBankDetector {

    public BankType detectBankType(String[] headers) {
        Set<String> headerSet = Arrays.stream(headers)
                .map(String::toLowerCase)
                .map(String::trim)
                .collect(Collectors.toSet());

        if (headerSet.contains("date") && headerSet.contains("description") &&
                headerSet.contains("amount")) {
            return BankType.BANK_OF_AMERICA;
        }

        if (headerSet.contains("data") && headerSet.contains("histórico") &&
                headerSet.contains("valor")) {
            return BankType.BANCO_DO_BRASIL;
        }

        throw new IllegalArgumentException("Unsupported CSV format. Unable to detect bank type from headers: " +
                String.join(", ", headers));
    }

    public BankType detectBankTypeFromFile(MultipartFile file) throws IOException {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1);
                CSVReader csvReader = new CSVReader(reader)) {
            String[] headers = csvReader.readNext();

            if (headers == null || headers.length == 0) {
                throw new IllegalArgumentException("CSV file appears to be empty or has no headers");
            }

            return detectBankType(headers);
        } catch (CsvValidationException e) {
            throw new IllegalArgumentException("Invalid CSV format: " + e.getMessage(), e);
        }
    }
}
