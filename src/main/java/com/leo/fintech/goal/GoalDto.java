package com.leo.fintech.goal;

import java.math.BigDecimal;
import java.time.YearMonth;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDto {
    
    private Long id;
    
    private String name;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than or equal to 0.01")
    @DecimalMax(value = "1000000.00", message = "Amount must be less than or equal to 1,000,000.00")
    @Digits(integer = 7, fraction = 2, message = "Amount must have up to 7 digits before the decimal and 2 digits after")
    private BigDecimal targetAmount;
    
    private BigDecimal currentAmount;
    
    private YearMonth dueMonth;
    
    private Long categoryId;
    
    private Long accountId;
    
    private Long userId;
}