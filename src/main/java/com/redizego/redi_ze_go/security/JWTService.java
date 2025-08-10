package com.redizego.redi_ze_go.security;

import com.redizego.redi_ze_go.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service for handling JWT (JSON Web Token) operations in the authentication system.
 * 
 * This service provides functionality for:
 * - Generating access tokens for API authentication
 * - Generating refresh tokens for token renewal
 * - Parsing and validating JWT tokens
 * - Extracting user information from tokens
 * 
 * Security Features:
 * - Uses HMAC-SHA256 algorithm for token signing
 * - Configurable secret key from application properties
 * - Separate token types with different expiration times
 * - Comprehensive error handling for token validation
 * 
 * Token Structure:
 * - Access Token: Contains user ID, email, roles (10 minutes expiry)
 * - Refresh Token: Contains only user ID (24 hours expiry)
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see com.redizego.redi_ze_go.security.JwtAuthFilter
 * @see com.redizego.redi_ze_go.controllers.AuthController
 */
@Slf4j
@Service
public class JWTService {

    /** 
     * JWT secret key loaded from application properties.
     * Used for signing and verifying JWT tokens.
     */
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    /** Access token expiration time in milliseconds (10 minutes) */
    private static final long ACCESS_TOKEN_EXPIRY = 1000 * 60 * 10;
    
    /** Refresh token expiration time in milliseconds (24 hours) */
    private static final long REFRESH_TOKEN_EXPIRY = 1000 * 60 * 60 * 24;

    /**
     * Creates a SecretKey instance from the configured JWT secret.
     * 
     * This method converts the string secret key to a proper SecretKey object
     * using HMAC-SHA256 algorithm. The key is used for both signing and
     * verifying JWT tokens.
     * 
     * @return SecretKey instance for JWT operations
     * @throws IllegalArgumentException if secret key is too short or invalid
     * 
     * @implNote Secret key should be at least 256 bits (32 bytes) for HS256
     */
    private SecretKey getSecretKey() {
        try {
            return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to create secret key from JWT secret: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT secret key", e);
        }
    }

    /**
     * Generates an access token for the given user.
     * 
     * Access tokens are used for API authentication and contain user details
     * including ID, email, and roles. They have a short expiration time (10 minutes)
     * to minimize security risks if compromised.
     * 
     * Token claims:
     * - sub: User ID (subject)
     * - email: User's email address
     * - role: User's roles (RIDER, DRIVER, ADMIN)
     * - iat: Issued at timestamp
     * - exp: Expiration timestamp
     * 
     * @param user The user for whom to generate the access token
     * @return JWT access token as a string
     * @throws IllegalArgumentException if user is null or has invalid data
     * 
     * @implNote Token is signed with HMAC-SHA256 algorithm
     */
    public String generateAccessToken(User user) {
        if (user == null) {
            log.error("Cannot generate access token: user is null");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getId() == null) {
            log.error("Cannot generate access token: user ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        try {
            Date issuedAt = new Date();
            Date expiration = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY);
            
            String token = Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .claim("role", user.getRole())
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(getSecretKey())
                    .compact();
            
            log.debug("Generated access token for user ID: {} with expiration: {}", 
                    user.getId(), expiration);
            
            return token;
            
        } catch (Exception e) {
            log.error("Failed to generate access token for user ID: {}", user.getId(), e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * Generates a refresh token for the given user.
     * 
     * Refresh tokens are used to obtain new access tokens without requiring
     * the user to log in again. They have a longer expiration time (24 hours)
     * and contain minimal information for security.
     * 
     * Token claims:
     * - sub: User ID (subject)
     * - iat: Issued at timestamp
     * - exp: Expiration timestamp
     * 
     * @param user The user for whom to generate the refresh token
     * @return JWT refresh token as a string
     * @throws IllegalArgumentException if user is null or has invalid data
     * 
     * @implNote Refresh tokens should be stored in HTTP-only cookies
     * @implNote Contains minimal user information for security
     */
    public String generateRefreshToken(User user) {
        if (user == null) {
            log.error("Cannot generate refresh token: user is null");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getId() == null) {
            log.error("Cannot generate refresh token: user ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        try {
            Date issuedAt = new Date();
            Date expiration = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY);
            
            String token = Jwts.builder()
                    .subject(user.getId().toString())
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(getSecretKey())
                    .compact();
            
            log.debug("Generated refresh token for user ID: {} with expiration: {}", 
                    user.getId(), expiration);
            
            return token;
            
        } catch (Exception e) {
            log.error("Failed to generate refresh token for user ID: {}", user.getId(), e);
            throw new RuntimeException("Refresh token generation failed", e);
        }
    }

    /**
     * Extracts the user ID from a JWT token.
     * 
     * This method parses and validates the given JWT token, then extracts
     * the user ID from the token's subject claim. The token signature is
     * verified using the secret key.
     * 
     * @param token The JWT token to parse
     * @return The user ID extracted from the token
     * @throws JwtException if token is invalid, expired, or malformed
     * @throws IllegalArgumentException if token is null or empty
     * 
     * @implNote This method validates token signature and expiration
     * @implNote Used by JwtAuthFilter for authentication
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.error("Cannot extract user ID: token is null or empty");
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String subject = claims.getSubject();
            if (subject == null) {
                log.error("Token does not contain a subject (user ID)");
                throw new JwtException("Invalid token: no subject found");
            }
            
            Long userId = Long.valueOf(subject);
            log.debug("Successfully extracted user ID: {} from token", userId);
            
            return userId;
            
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format in token subject: {}", e.getMessage());
            throw new JwtException("Invalid token: user ID format error", e);
            
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("Unexpected error during token parsing: {}", e.getMessage(), e);
            throw new JwtException("Token parsing failed", e);
        }
    }
    
    /**
     * Validates if a token is expired.
     * 
     * @param token The JWT token to validate
     * @return true if token is expired, false otherwise
     * @throws JwtException if token is malformed or invalid
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Date expiration = claims.getExpiration();
            boolean expired = expiration.before(new Date());
            
            if (expired) {
                log.debug("Token is expired. Expiration: {}, Current: {}", 
                        expiration, new Date());
            }
            
            return expired;
            
        } catch (JwtException e) {
            log.error("Error validating token expiration: {}", e.getMessage());
            throw e;
        }
    }
}
