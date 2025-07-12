package com.leo.fintech.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AccountDto {
    private Long id;
    private String name;
    private String type;
    private String institution;
    private BigDecimal balance;
    private LocalDate createdAt;
    private UUID userId;
}
