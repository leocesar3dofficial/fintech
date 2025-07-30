package com.leo.fintech.auth;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetToken);
}
