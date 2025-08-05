package com.leo.fintech.email;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetToken);
}
