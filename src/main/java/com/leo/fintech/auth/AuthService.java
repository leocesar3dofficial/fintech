package com.leo.fintech.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.leo.fintech.common.exception.EmailAlreadyExistsException;
import com.leo.fintech.common.exception.InvalidPasswordException;
import com.leo.fintech.common.exception.InvalidTokenException;
import com.leo.fintech.common.exception.UserNotFoundException;
import com.leo.fintech.email.EmailService;
import com.leo.fintech.user.CustomUserDetails;
import com.leo.fintech.user.User;
import com.leo.fintech.user.UserDto;
import com.leo.fintech.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {

            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        userRepository.save(user);
        String jwt = jwtService.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getUsername(),
                user.getRole());

        return new AuthResponse(jwt);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String jwt = jwtService.generateToken(
                    user.getId().toString(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole());

            return new AuthResponse(jwt);
        } catch (AuthenticationException ex) {
            return new AuthResponse("Invalid email or password.");
        }
    }

    public UserDto getUserDto(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            User user = customUserDetails.getUser();

            return new UserDto(user.getUsername(), user.getEmail());

        } else if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {

            return new UserDto(jwtUserPrincipal.getUsername(), jwtUserPrincipal.getEmail());
        }

        String fallback = principal instanceof UserDetails
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        return new UserDto(fallback, fallback);
    }

    @Transactional
    public void deleteUser(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            User user = customUserDetails.getUser();
            userRepository.deleteById(user.getId());
        } else if (principal instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            userRepository.findByEmail(email).ifPresent(user -> userRepository.deleteById(user.getId()));
        } else if (principal instanceof String str) {
            userRepository.findByEmail(str).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        User user = userOpt.get();
        String resetToken = jwtService.generatePasswordResetToken(
                user.getId().toString(),
                user.getEmail());
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(PasswordChangeRequest request) {
        if (!jwtService.isPasswordResetTokenValid(request.getToken())) {
            throw new InvalidTokenException("Invalid or expired reset token");
        }

        try {
            String email = jwtService.extractEmailFromResetToken(request.getToken());
            String userId = jwtService.extractUserIdFromResetToken(request.getToken());
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (!user.getId().toString().equals(userId)) {
                throw new InvalidTokenException("Token user mismatch");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            log.info("Password successfully reset for user: {}", user.getEmail());
        } catch (Exception e) {
            if (e instanceof InvalidTokenException || e instanceof UserNotFoundException) {
                throw e;
            }

            throw new InvalidTokenException("Invalid reset token");
        }
    }

    @Transactional
    public void updatePassword(String userEmail, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", user.getEmail());
    }
}
