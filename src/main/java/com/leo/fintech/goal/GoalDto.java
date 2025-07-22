package com.leo.fintech.goal;

import java.math.BigDecimal;
import java.time.YearMonth;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Name is required")
    @Size(min = 5, message = "Name must have at least 5 characters")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than or equal to 0.01")
    @DecimalMax(value = "1000000.00", message = "Amount must be less than or equal to 1,000,000.00")
    @Digits(integer = 7, fraction = 2, message = "Amount must have up to 7 digits before the decimal and 2 digits after")
    private BigDecimal targetAmount;

    @NotNull(message = "Due month is required")
    private YearMonth dueMonth;

    @NotNull(message = "Account ID is required")
    private Long accountId;
}