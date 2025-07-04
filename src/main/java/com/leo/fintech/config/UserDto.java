package com.leo.fintech.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class UserDto {
    private String username;
    private String email;
}