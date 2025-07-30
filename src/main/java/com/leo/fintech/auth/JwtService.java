package com.leo.fintech.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.reset-token.expiration:3600}")
    private long RESET_TOKEN_EXPIRATION;

    public String generateToken(String userId, String email, String username, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generatePasswordResetToken(String userId, String email) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("type", "PASSWORD_RESET") // Token type claim
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(RESET_TOKEN_EXPIRATION, ChronoUnit.SECONDS)))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isPasswordResetTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get("type", String.class);
            if (!"PASSWORD_RESET".equals(tokenType)) {
                return false;
            }

            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmailFromResetToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    public String extractUserIdFromResetToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("username", String.class);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}
