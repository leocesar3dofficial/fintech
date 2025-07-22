package com.leo.fintech.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 5, message = "Username must have at least 5 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @ValidPassword(minLength = 8, requireNumber = true, requireUppercase = true, requireSpecial = true, message = "Password does not meet security requirements")
    private String password;
}