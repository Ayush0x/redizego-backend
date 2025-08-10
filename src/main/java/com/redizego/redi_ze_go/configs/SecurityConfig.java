package com.redizego.redi_ze_go.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration class for password encoding and authentication management.
 * 
 * This configuration class provides essential security beans used throughout
 * the application for user authentication and password security.
 * 
 * Key Features:
 * - BCrypt password encoder with default strength (10 rounds)
 * - Authentication manager for handling login processes
 * - Secure password hashing for user data protection
 * 
 * Security Considerations:
 * - BCrypt is used for its adaptive nature and salt generation
 * - Default BCrypt strength provides good balance of security and performance
 * - Authentication manager integrates with Spring Security's authentication flow
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see WebSecurityConfig
 * @see com.redizego.redi_ze_go.security.JwtAuthFilter
 * @see com.redizego.redi_ze_go.services.impl.AuthServiceImpl
 */
@Slf4j
@Configuration
public class SecurityConfig {

    /**
     * Provides a BCrypt password encoder bean for secure password hashing.
     * 
     * BCrypt is a password hashing function designed by Niels Provos and
     * David Mazières. It's based on the Blowfish cipher and includes a salt
     * to protect against rainbow table attacks.
     * 
     * Features:
     * - Adaptive cost parameter (default: 10 rounds)
     * - Automatic salt generation for each password
     * - Resistance to timing attacks
     * - Industry-standard security for password storage
     * 
     * @return BCryptPasswordEncoder instance with default strength
     * 
     * @implNote Default strength of 10 provides good security-performance balance
     * @implNote Each password encoding generates a unique salt automatically
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Initializing BCryptPasswordEncoder with default strength");
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides an AuthenticationManager bean for handling authentication processes.
     * 
     * The AuthenticationManager is the main strategy interface for authentication
     * in Spring Security. It coordinates the authentication process and delegates
     * to configured AuthenticationProviders.
     * 
     * This manager is used by:
     * - AuthService for login operations
     * - Spring Security for processing authentication requests
     * - JWT authentication flows
     * 
     * @param authenticationConfiguration Spring's authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if authentication manager cannot be created
     * 
     * @implNote Uses Spring Boot's default authentication configuration
     * @implNote Integrates with UserDetailsService for user loading
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        
        log.debug("Configuring AuthenticationManager from AuthenticationConfiguration");
        
        try {
            AuthenticationManager authManager = authenticationConfiguration.getAuthenticationManager();
            log.info("AuthenticationManager successfully configured");
            return authManager;
            
        } catch (Exception e) {
            log.error("Failed to configure AuthenticationManager: {}", e.getMessage(), e);
            throw e;
        }
    }
}
