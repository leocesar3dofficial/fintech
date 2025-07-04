package com.leo.fintech.config;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.leo.fintech.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new AuthResponse("Email already in use");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        userRepository.save(user);
        String jwt = jwtService.generateToken(user.getEmail());
        return new AuthResponse(jwt);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String jwt = jwtService.generateToken(user.getEmail());
            return new AuthResponse(jwt);
        } catch (AuthenticationException ex) {
            return new AuthResponse("Invalid email or password.");
        }
    }

    public UserDto getUserDto(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof com.leo.fintech.config.User user) {
            return new UserDto(user.getUsername(), user.getEmail());
        }

        // fallback: UserDetails or String (username only, email unknown)
        String fallback = principal instanceof UserDetails
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        return new UserDto(fallback, fallback); // No way to know real email & username from just a string
    }
}
