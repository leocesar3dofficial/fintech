package com.leo.fintech.auth;

import java.io.Serializable;

public class JwtUserPrincipal implements Serializable {
    private final String userId;
    private final String email;
    private final String username;
    private final String role;

    public JwtUserPrincipal(String userId, String email, String username, String role) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
