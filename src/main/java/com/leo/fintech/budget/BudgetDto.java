package com.leo.fintech.budget;

import java.math.BigDecimal;
import java.time.YearMonth;
import com.leo.fintech.category.Category;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {

    private Long id;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than or equal to 0.01")
    @DecimalMax(value = "1000000.00", message = "Amount must be less than or equal to 1,000,000.00")
    @Digits(integer = 7, fraction = 2, message = "Amount must have up to 7 digits before the decimal and 2 digits after")
    private BigDecimal amount;
    
    @NotNull(message = "Month is required")
    private YearMonth month;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}