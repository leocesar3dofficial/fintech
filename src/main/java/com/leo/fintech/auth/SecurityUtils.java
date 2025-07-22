package com.leo.fintech.auth;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    // Private constructor to prevent instantiation
    private SecurityUtils() {
    }

    public static UUID extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object userPrincipal = authentication.getPrincipal();
        if (userPrincipal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return UUID.fromString(jwtUserPrincipal.getUserId());
        }

        throw new IllegalArgumentException(
                "Invalid principal type: " + (userPrincipal != null ? userPrincipal.getClass() : "null"));
    }

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
