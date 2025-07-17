package com.leo.fintech.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto {
   
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 5, message = "Name must have at least 5 characters")
    private String name;

    @NotBlank(message = "Type is required")
    @Size(min = 5, message = "Type must have at least 5 characters")
    private String type;
    
    @NotBlank(message = "Institution is required")
    @Size(min = 3, message = "Institution must have at least 3 characters")
    private String institution;

    private BigDecimal balance;

    private LocalDate createdAt;

    private UUID userId;
}
