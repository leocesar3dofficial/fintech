package com.leo.fintech.config;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}