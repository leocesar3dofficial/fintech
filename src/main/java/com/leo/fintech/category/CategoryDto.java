package com.leo.fintech.category;

import java.util.UUID;

import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 5, message = "Name must have at least 5 characters")
    private String name;

    @NotNull(message = "Is income is required")
    private Boolean isIncome;

    private UUID userId;
}
