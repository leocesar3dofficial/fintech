package com.leo.fintech.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.reset-token.expiration}")
    private long RESET_TOKEN_EXPIRATION;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public String generateToken(String userId, String email, String username, String role) {
        return createToken(userId, email, username, role, null,
                Instant.now().plus(1, ChronoUnit.DAYS));
    }

    public String generatePasswordResetToken(String userId, String email) {
        return createToken(userId, email, null, null, "PASSWORD_RESET",
                Instant.now().plus(RESET_TOKEN_EXPIRATION, ChronoUnit.SECONDS));
    }

    private String createToken(String userId, String email, String username,
            String role, String type, Instant expiration) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256);

        if (username != null)
            builder.claim("username", username);
        if (role != null)
            builder.claim("role", role);
        if (type != null)
            builder.claim("type", type);

        return builder.compact();
    }

    public boolean isPasswordResetTokenValid(String token) {
        Claims claims = extractClaimsOrNull(token);
        if (claims == null)
            return false;

        String tokenType = claims.get("type", String.class);
        return "PASSWORD_RESET".equals(tokenType) && !isExpired(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null &&
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extractClaimsOrNull(token);
        return claims == null || isExpired(claims);
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserIdFromResetToken(String token) {
        return extractUserId(token); // Same implementation
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractEmailFromResetToken(String token) {
        return extractEmail(token); // Same implementation
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaimsOrNull(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    private Claims extractClaimsOrNull(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.debug("Token expired: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.debug("Error extracting claims from token: {}", e.getMessage());
            return null;
        }
    }
}