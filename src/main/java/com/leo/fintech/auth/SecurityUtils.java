package com.leo.fintech.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    // Private constructor to prevent instantiation
    // This class provides utility methods related to security and authentication
    private SecurityUtils() {}

    /**
     * Extracts the userId from the current authenticated principal.
     * Throws IllegalArgumentException if the principal is not a JwtUserPrincipal.
     */
    public static String extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        Object userPrincipal = authentication.getPrincipal();
        if (userPrincipal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.getUserId();
        }
        throw new IllegalArgumentException("Invalid principal type: " + (userPrincipal != null ? userPrincipal.getClass() : "null"));
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
