package com.leo.fintech.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.leo.fintech.exception.EmailAlreadyExistsException;

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
            throw new EmailAlreadyExistsException(request.getEmail());
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

        if (principal instanceof CustomUserDetails customUserDetails) {
            com.leo.fintech.auth.User user = customUserDetails.getUser();
            return new UserDto(user.getUsername(), user.getEmail());
        }

        // fallback: UserDetails or String (username only, email unknown)
        String fallback = principal instanceof UserDetails
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        return new UserDto(fallback, fallback); // No way to know real email & username from just a string
    }
    
    /**
     * Deletes the currently authenticated user from the database.
     * @param authentication the current authentication object
     */
    public void deleteUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            User user = customUserDetails.getUser();
            userRepository.deleteById(user.getId());
        } else if (principal instanceof UserDetails userDetails) {
            // fallback: try to find by email/username
            String email = userDetails.getUsername();
            userRepository.findByEmail(email).ifPresent(user -> userRepository.deleteById(user.getId()));
        } else if (principal instanceof String str) {
            // fallback: try to find by string username
            userRepository.findByEmail(str).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
    }
}
