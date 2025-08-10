package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import java.util.Arrays;

/**
 * REST controller for handling authentication and authorization operations.
 * 
 * This controller provides endpoints for user registration, login, driver onboarding,
 * and token management. It implements JWT-based authentication with refresh token
 * support using HTTP-only cookies for enhanced security.
 * 
 * Key Features:
 * - User signup and login
 * - JWT access and refresh token management
 * - Driver onboarding process
 * - Role-based access control
 * - Secure cookie-based refresh token storage
 * 
 * Security Considerations:
 * - Refresh tokens stored in HTTP-only cookies to prevent XSS
 * - Access tokens returned in response body for API usage
 * - Password encryption using BCrypt
 * - Role-based authorization for sensitive operations
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see AuthService
 * @see com.redizego.redi_ze_go.security.JWTService
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     * 
     * Creates a new user account with RIDER role by default. Also initializes
     * a rider profile and wallet for the new user. Passwords are encrypted
     * using BCrypt before storage.
     * 
     * @param signupDto Contains user registration information (name, email, password)
     * @return ResponseEntity with UserDto containing created user details
//     * @throws RuntimeConflictException if user with email already exists
     * 
     * @apiNote All new users start with RIDER role; drivers must be onboarded separately
     */
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupDto signupDto) {
        log.info("User signup attempt for email: {}", signupDto.getEmail());
        
        UserDto createdUser = authService.signup(signupDto);
        
        log.info("Successfully created user with ID: {} and email: {}", 
                createdUser.getId(), createdUser.getEmail());
        
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Onboards an existing user as a driver.
     * 
     * Converts a regular user (RIDER) into a driver by adding the DRIVER role
     * and creating a driver profile. Users can have both RIDER and DRIVER roles.
     * 
     * @param userId The ID of the user to onboard as driver
     * @param onboardDriverDto Contains driver-specific information (vehicle ID)
     * @return ResponseEntity with DriverDto containing driver profile details
//     * @throws ResourceNotFoundException if user with given ID doesn't exist
//     * @throws RuntimeConflictException if user is already a driver
     * 
     * @apiNote Currently commented out security annotation for testing purposes
     * @todo Uncomment @Secured("ROLE_ADMIN") for production
     */
    // @Secured("ROLE_ADMIN") // TODO: Uncomment for production
    @PostMapping("/onboard-driver/{user-id}")
    public ResponseEntity<DriverDto> onboardDriver(
            @PathVariable("user-id") Long userId, 
            @RequestBody OnboardDriverDto onboardDriverDto) {
        
        log.info("Driver onboarding request for user ID: {} with vehicle: {}", 
                userId, onboardDriverDto.getVehicleId());
        
        DriverDto driverDto = authService.onboardDriver(userId, onboardDriverDto.getVehicleId());
        
        log.info("Successfully onboarded driver with ID: {} for user: {}", 
                driverDto.getId(), userId);
        
        return new ResponseEntity<>(driverDto, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and provides access and refresh tokens.
     * 
     * Validates user credentials and returns JWT tokens for API access.
     * The access token is returned in the response body, while the refresh
     * token is set as an HTTP-only cookie for security.
     * 
     * @param loginRequestDto Contains login credentials (email and password)
     * @param request HTTP request for potential additional security checks
     * @param response HTTP response to set refresh token cookie
     * @return ResponseEntity with LoginResponseDto containing access token
//     * @throws AuthenticationException if credentials are invalid
     * 
     * @implNote Refresh token cookie is set as HTTP-only to prevent XSS attacks
     * @implNote Cookie security is currently disabled for development (setSecure(false))
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto, 
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        log.info("Login attempt for email: {}", loginRequestDto.getEmail());
        
        try {
            // Authenticate and get tokens
            String[] tokens = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            
            // Create HTTP-only cookie for refresh token
            Cookie refreshCookie = new Cookie("refreshToken", tokens[1]); // Note: should be "refreshToken", not "token"
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false); // TODO: Set to true in production with HTTPS
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            refreshCookie.setPath("/");
            
            response.addCookie(refreshCookie);
            
            log.info("Successful login for email: {}", loginRequestDto.getEmail());
            
            return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
            
        } catch (Exception e) {
            log.error("Login failed for email: {}", loginRequestDto.getEmail(), e);
            throw e;
        }
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     * 
     * Validates the refresh token from HTTP-only cookie and issues a new
     * access token if the refresh token is valid and not expired.
     * 
     * @param request HTTP request containing refresh token in cookies
     * @return ResponseEntity with LoginResponseDto containing new access token
//     * @throws AuthorizationServiceException if no cookies or refresh token found
//     * @throws ResourceNotFoundException if user associated with token not found
     * 
     * @implNote Refresh token must be present in HTTP-only cookie named "refreshToken"
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(HttpServletRequest request) {
        log.debug("Token refresh request received");
        
        if (request.getCookies() == null) {
            log.warn("Token refresh failed: No cookies found in request");
            throw new AuthorizationServiceException("No cookies found");
        }
        
        // Extract refresh token from cookies
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: Refresh token cookie not found");
                    return new AuthorizationServiceException("Refresh token not found");
                });
        
        try {
            // Generate new access token
            String accessToken = authService.refreshToken(refreshToken);
            
            log.debug("Successfully refreshed access token");
            
            return ResponseEntity.ok(new LoginResponseDto(accessToken));
            
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

}
