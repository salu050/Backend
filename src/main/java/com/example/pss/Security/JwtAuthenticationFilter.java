package com.example.pss.Security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException; // This specific import is for io.jsonwebtoken.security.SignatureException
import io.jsonwebtoken.UnsupportedJwtException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Use SLF4J for logging, which is the standard in Spring Boot
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Check if Authorization header exists and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract the token after "Bearer "
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token is expired: {}", e.getMessage());
                // Set response status to 401 Unauthorized for expired tokens.
                // This prevents further processing with an expired token.
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token is expired.");
                return; // Stop the filter chain
            } catch (SignatureException e) {
                logger.warn("Invalid JWT signature: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT signature.");
                return; // Stop the filter chain
            } catch (MalformedJwtException e) {
                logger.warn("Invalid JWT token format: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token format.");
                return; // Stop the filter chain
            } catch (UnsupportedJwtException e) {
                logger.warn("Unsupported JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unsupported JWT token.");
                return; // Stop the filter chain
            } catch (IllegalArgumentException e) {
                logger.warn("JWT claims string is empty or null: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT claims string is empty or null.");
                return; // Stop the filter chain
            } catch (Exception e) { // Catch any other unexpected exceptions during extraction
                logger.error("An unexpected error occurred while extracting username from JWT: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("An internal server error occurred.");
                return; // Stop the filter chain
            }
        } else {
            logger.debug("Authorization header does not begin with Bearer String or is null. Request for: {}",
                    request.getRequestURI());
        }

        // If username is extracted from the token and no authentication is currently
        // set in the SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = null;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                logger.error("Error loading user details for username {}: {}", username, e.getMessage());
                // If user details cannot be loaded, treat as unauthorized.
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User not found or unable to load user details.");
                return; // Stop the filter chain
            }

            // Validate the token against the user details
            // This checks if the username matches and if the token is not expired/invalid.
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // Create an authentication token with user details and their authorities
                // (roles)
                // IMPORTANT: userDetails.getAuthorities() must correctly return the roles
                // (e.g., ROLE_ADMIN) as populated by your CustomUserDetailsService.
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Set additional details from the request
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the SecurityContext.
                // This is crucial for Spring Security's @PreAuthorize annotations to work.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                logger.debug("User {} authenticated successfully.", username);
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
                // If token is invalid (e.g., username mismatch, or other validation failure in
                // JwtUtil)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired JWT token.");
                return; // Stop the filter chain
            }
        }

        // Continue the filter chain. If authentication was not set (e.g., no token, or
        // invalid token handled above),
        // subsequent filters (like AuthorizationFilter) will handle the unauthorized
        // access.
        filterChain.doFilter(request, response);
    }
}
