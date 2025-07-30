package com.example.pss.Security; // Adjust package name if different

import com.example.pss.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority; // Import for Spring Security authorities
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import for SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors; // Import for Collectors

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // in milliseconds

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key ('jwt.secret') must not be null or empty.");
        }
        // Ensure the secret is at least 256 bits (32 bytes) for HS256
        if (secret.length() < 32) {
            logger.warn(
                    "JWT secret key ('jwt.secret') is too short. It should be at least 32 characters (256 bits) for HS256 algorithm. Consider using a stronger, randomly generated key.");
        }
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
     * it adds 'userId' and 'roles' as custom claims, ensuring 'ROLE_' prefix.
     *
     * @param userDetails The UserDetails object representing the user.
     * @return The generated JWT token string.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("userId", user.getId());
            // FIX: Add 'ROLE_' prefix to the role before putting it into claims
            // Spring Security expects roles to be prefixed with "ROLE_"
            claims.put("roles", List.of("ROLE_" + user.getRole().name().toUpperCase()));
        } else {
            // If it's just a UserDetails (not your custom User model),
            // you might still want to add authorities.
            // This assumes UserDetails.getAuthorities() returns authorities without "ROLE_"
            // if your User model's getRole() is just "ADMIN".
            // If your UserDetails implementation already provides "ROLE_ADMIN", this might
            // need adjustment.
            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            claims.put("roles", authorities);
        }
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates the JWT token using the provided claims and subject.
     * This method has been updated to use the modern JJWT 0.12.x+ API.
     *
     * @param claims  The custom claims to include in the token payload.
     * @param subject The subject (username) of the token.
     * @return The compact JWT token string.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Validates a given JWT token against the provided UserDetails.
     * This method now includes more specific exception handling for different JWT
     * errors.
     *
     * @param token       The JWT token string to validate.
     * @param userDetails The UserDetails object to validate against.
     * @return true if the token is valid for the user and not expired, false
     *         otherwise.
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
            logger.error("JWT validation failed: Invalid token argument (e.g., empty or null). Error: {}",
                    e.getMessage());
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
     * @param token          The JWT token string.
     * @param claimsResolver A function to resolve the desired claim from the Claims
     *                       object.
     * @param <T>            The type of the claim to be extracted.
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
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the 'role' claim from a JWT token.
     * NOTE: This method extracts a single 'role' string. If your token stores roles
     * as a List,
     * you might need `extractRoles` instead.
     *
     * @param token The JWT token string.
     * @return The role string, or null if not found.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Helper to extract authorities (roles) from the token.
     * This is crucial for Spring Security's authentication process.
     * The roles are expected to be stored as a List<String> in the "roles" claim.
     *
     * @param token The JWT token string.
     * @return A List of GrantedAuthority objects.
     */
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        // Ensure the claim name matches what you put in generateToken (e.g., "roles")
        List<?> rolesObject = claims.get("roles", List.class);
        if (rolesObject != null) {
            return rolesObject.stream()
                    .filter(String.class::isInstance) // Ensure elements are strings
                    .map(String.class::cast) // Cast to String
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return List.of(); // Return empty list if no roles found
    }
}
