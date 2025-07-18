package com.leo.fintech.converter;

import java.sql.Date;
import java.time.YearMonth;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Set to false so you can apply it explicitly
@Converter(autoApply = false) 
public class YearMonthDateAttributeConverter implements AttributeConverter<YearMonth, Date> {

    @Override
    public Date convertToDatabaseColumn(YearMonth yearMonth) {
        return yearMonth != null ? Date.valueOf(yearMonth.atDay(1)) : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(Date date) {
        return date != null ? YearMonth.from(date.toLocalDate()) : null;
    }
}