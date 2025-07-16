package com.leo.fintech.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    // Private constructor to prevent instantiation
    // This class provides utility methods related to security and authentication
    private SecurityUtils() {}

    public static Object getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtUserPrincipal) {
            return principal;
        } else if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }
}
