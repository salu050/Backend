package com.example.pss.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.security.Key; // Still needed for general reference if you were to use it broadly, but now SecretKey is more specific for 'key' field
import javax.crypto.SecretKey; // <-- NEW IMPORT: This is the specific type needed for verifyWith(SecretKey)
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.example.pss.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // in milliseconds

    private SecretKey key; // <-- FIX: Changed from 'Key' to 'SecretKey'

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key ('jwt.secret') must not be null or empty.");
        }
        if (secret.length() < 32) {
            logger.warn(
                    "JWT secret key ('jwt.secret') is too short. It should be at least 32 characters (256 bits) for HS256 algorithm. Consider using a stronger, randomly generated key.");
        }
        // Keys.hmacShaKeyFor returns a SecretKey, so assigning it to 'SecretKey' type is correct.
        this.key = Keys.hmacShaKeyFor(secret.getBytes());

        if (expiration <= 0) {
            logger.warn(
                    "JWT token expiration ('jwt.expiration') is set to {}ms. Tokens will expire immediately or are invalid.",
                    expiration);
        }
    }

    /**
     * Generates a JWT token for the given UserDetails.
     * If the UserDetails is an instance of 'User' (from your model),
     * it adds 'userId' and 'role' as custom claims.
     *
     * @param userDetails The UserDetails object representing the user.
     * @return The generated JWT token string.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("userId", user.getId());
            claims.put("role", user.getRole().name());
        }
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates the JWT token using the provided claims and subject.
     * This method has been updated to use the modern JJWT 0.12.x+ API.
     *
     * @param claims The custom claims to include in the token payload.
     * @param subject The subject (username) of the token.
     * @return The compact JWT token string.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key) // Now 'key' is explicitly SecretKey, matching the expected type.
                .compact();
    }

    /**
     * Validates a given JWT token against the provided UserDetails.
     * This method now includes more specific exception handling for different JWT errors.
     *
     * @param token The JWT token string to validate.
     * @param userDetails The UserDetails object to validate against.
     * @return true if the token is valid for the user and not expired, false otherwise.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            logger.error("JWT validation failed: Token expired. Error: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT validation failed: Token is unsupported. Error: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("JWT validation failed: Token is malformed. Error: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.error("JWT validation failed: Invalid signature. Error: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT validation failed: Invalid token argument (e.g., empty or null). Error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during token validation for token: {}. Error: {}", token,
                    e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token The JWT token string.
     * @return The username.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT token string.
     * @return The expiration Date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token The JWT token string.
     * @return true if the token is expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts a specific claim from a JWT token using a ClaimsResolver function.
     *
     * @param token The JWT token string.
     * @param claimsResolver A function to resolve the desired claim from the Claims object.
     * @param <T> The type of the claim to be extracted.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token using the updated JJWT API for 0.12.x+.
     *
     * @param token The JWT token string.
     * @return The claims extracted from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key) // This now correctly accepts 'SecretKey'
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the 'role' claim from a JWT token.
     *
     * @param token The JWT token string.
     * @return The role string, or null if not found.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}
