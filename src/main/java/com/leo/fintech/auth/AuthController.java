package com.leo.fintech.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(errorMsg));
    }

    @Autowired
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getUserDto(authentication));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        authService.deleteUser(authentication);
        return ResponseEntity.ok("Usuário deletado com sucesso.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "If the email exists, a password reset link has been sent");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.resetPassword(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password has been reset successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request) {
        
        String userEmail = authentication.getName(); // Assuming email is used as username
        authService.updatePassword(userEmail, request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password updated successfully");
        
        return ResponseEntity.ok(response);
    }
}
