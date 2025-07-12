package com.leo.fintech.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    // Private constructor to prevent instantiation
    // This class provides utility methods related to security and authentication
    private SecurityUtils() {}

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("No authenticated user found");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }
}
