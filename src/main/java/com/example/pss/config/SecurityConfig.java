package com.example.pss.config;

import com.example.pss.Security.CustomUserDetailsService;
import com.example.pss.Security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// No need to import CorsFilter if you're using the HttpSecurity.cors() method
// import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize and @PostAuthorize annotations
public class SecurityConfig {

    // Use constructor injection for dependencies (Spring handles @Autowired
    // implicitly here)
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Password Encoder Bean: Uses BCrypt for strong password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Manager Bean: Exposes the AuthenticationManager for use in
    // controllers (e.g., login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // DAO Authentication Provider: Configures how Spring Security fetches user
    // details and verifies passwords
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService); // Set your custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Set the password encoder
        return authProvider;
    }

    // CORS Configuration Source Bean: Defines the Cross-Origin Resource Sharing
    // rules
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Allow sending of cookies, authorization headers etc.
        config.addAllowedOrigin("http://localhost:3000"); // Allow your frontend origin
        // In production, replace "http://localhost:3000" with your actual frontend
        // domain(s).
        config.addAllowedHeader("*"); // Allow all headers
        config.addAllowedMethod("*"); // Allow all HTTP methods (GET, POST, PUT, DELETE, OPTIONS, etc.)
        source.registerCorsConfiguration("/**", config); // Apply this CORS configuration to all paths
        return source;
    }

    // Security Filter Chain Configuration: Defines the security rules and filter
    // order
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs using JWT
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS configuration
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints that do not require authentication
                        .requestMatchers("/api/users/login", "/api/users/register", "/api/users/reset-password")
                        .permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        // Set session creation policy to STATELESS as JWTs are self-contained and don't
                        // rely on server-side sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider()) // Register the custom authentication provider
                // Add the JWT authentication filter before Spring Security's default
                // UsernamePasswordAuthenticationFilter
                // This ensures JWT validation happens first for authenticated requests.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Build the security filter chain
    }
}