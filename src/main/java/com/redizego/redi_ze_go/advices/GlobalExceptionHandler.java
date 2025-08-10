package com.redizego.redi_ze_go.advices;

import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.exceptions.RuntimeConflictException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Redizego ride-hailing application.
 * 
 * This class provides centralized exception handling across all REST controllers
 * using Spring's @RestControllerAdvice annotation. It intercepts exceptions thrown
 * by any controller method and converts them into standardized API error responses.
 * 
 * Key Features:
 * - Centralized exception handling for consistent error responses
 * - HTTP status code mapping based on exception type
 * - Detailed error messages with validation details
 * - Structured error response format using ApiError and ApiResponse
 * - Comprehensive logging for debugging and monitoring
 * 
 * Exception Mappings:
 * - ResourceNotFoundException -> 404 NOT_FOUND
 * - RuntimeConflictException -> 409 CONFLICT
 * - MethodArgumentNotValidException -> 400 BAD_REQUEST
 * - AuthenticationException -> 401 UNAUTHORIZED
 * - JwtException -> 401 UNAUTHORIZED
 * - AccessDeniedException -> 403 FORBIDDEN
 * - Exception (catch-all) -> 500 INTERNAL_SERVER_ERROR
 * 
 * Response Format:
 * All exceptions are wrapped in ApiResponse<ApiError> with consistent structure
 * including timestamp, error details, and HTTP status information.
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see ApiError
 * @see ApiResponse
 * @see ResourceNotFoundException
 * @see RuntimeConflictException
 * 
 * @implNote Uses @RestControllerAdvice to apply globally to all REST controllers
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns 404 NOT_FOUND response.
     * 
     * This handler is triggered when requested resources (users, rides, drivers, etc.)
     * are not found in the system. It creates a standardized error response with
     * appropriate HTTP status and error message.
     * 
     * @param ex The ResourceNotFoundException containing error details
     * @return ResponseEntity with ApiError wrapped in ApiResponse and 404 status
     * 
     * @example When user tries to access non-existent ride: "Ride not found with ID: 123"
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ApiError error = ApiError.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build();
        
        return buildErrorResponseEntity(error);
    }

    /**
     * Handles generic Exception and returns 500 INTERNAL_SERVER_ERROR response.
     * 
     * This is a catch-all handler for unexpected exceptions that are not handled
     * by other specific handlers. It logs the full stack trace for debugging
     * and returns a generic error message to avoid exposing internal details.
     * 
     * @param ex The generic Exception
     * @return ResponseEntity with ApiError wrapped in ApiResponse and 500 status
     * 
     * @implNote This should be the last resort handler with lowest priority
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternalServerException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ApiError error = ApiError.builder()
                .message("An unexpected error occurred. Please try again later.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        
        return buildErrorResponseEntity(error);
    }

    /**
     * Handles MethodArgumentNotValidException and returns 400 BAD_REQUEST response.
     * 
     * This handler is triggered when request validation fails (e.g., @Valid annotations).
     * It extracts all validation error messages and returns them as a list for
     * better user experience and debugging.
     * 
     * @param ex The MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with detailed validation errors and 400 status
     * 
     * @implNote Collects all field validation errors into a single response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleInputValidationException(MethodArgumentNotValidException ex) {
        log.warn("Input validation failed with {} errors", ex.getBindingResult().getErrorCount());
        
        // Extract all validation error messages
        List<String> errors = ex
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        log.debug("Validation errors: {}", errors);

        ApiError error = ApiError.builder()
                .message("Validation failed")
                .status(HttpStatus.BAD_REQUEST)
                .errors(errors)
                .build();
        
        return buildErrorResponseEntity(error);
    }

    /**
     * Handles RuntimeConflictException and returns 409 CONFLICT response.
     * 
     * This handler is triggered when business rule violations occur, such as
     * attempting to create duplicate resources or perform conflicting operations.
     * 
     * @param ex The RuntimeConflictException containing conflict details
     * @return ResponseEntity with conflict error details and 409 status
     * 
     * @example When trying to create user with existing email
     * @example When driver tries to accept multiple rides simultaneously
     */
    @ExceptionHandler(RuntimeConflictException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeConflictException(RuntimeConflictException ex) {
        log.warn("Business rule conflict: {}", ex.getMessage());
        
        ApiError error = ApiError.builder()
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT)
                .build();
        
        return buildErrorResponseEntity(error);
    }

    /**
     * Handles AuthenticationException and returns 401 UNAUTHORIZED response.
     * 
     * This handler is triggered when authentication fails, such as invalid
     * credentials during login or expired authentication tokens.
     * 
     * @param ex The AuthenticationException containing authentication error details
     * @return ResponseEntity with authentication error and 401 status
     * 
     * @implNote Does not log sensitive authentication details for security
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        
        ApiError error = ApiError.builder()
                .message("Authentication failed. Please check your credentials.")
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        
        return buildErrorResponseEntity(error);
    }

    /**
     * Handles JwtException and returns 401 UNAUTHORIZED response.
     * 
     * This handler is triggered when JWT token validation fails, including
     * expired tokens, malformed tokens, or invalid signatures.
     * 
     * @param ex The JwtException containing JWT-specific error details
     * @return ResponseEntity with JWT authentication error and 401 status
     * 
     * @implNote Client should refresh token or re-authenticate
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException ex) {
        log.warn("JWT validation failed: {}", ex.getMessage());
        
        ApiError error = ApiError.builder()
                .message("Authentication token is invalid or expired. Please login again.")
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        
        return buildErrorResponseEntity(error);
    }

    /**
     * Handles AccessDeniedException and returns 403 FORBIDDEN response.
     * 
     * This handler is triggered when authenticated users try to access resources
     * they don't have permission for (e.g., wrong role, insufficient privileges).
     * 
     * @param ex The AccessDeniedException containing access denial details
     * @return ResponseEntity with access denied error and 403 status
     * 
     * @example When RIDER tries to access DRIVER-only endpoints
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ApiError error = ApiError.builder()
                .message("Access denied. You don't have permission to access this resource.")
                .status(HttpStatus.FORBIDDEN)
                .build();
        
        return buildErrorResponseEntity(error);
    }

    /**
     * Builds a standardized error response entity from ApiError.
     * 
     * This helper method creates a consistent response format for all error types,
     * wrapping the ApiError in an ApiResponse and setting the appropriate HTTP status.
     * 
     * @param error The ApiError containing error details and status
     * @return ResponseEntity with ApiResponse containing the error and matching HTTP status
     * 
     * @implNote Ensures consistent error response format across all handlers
     */
    private ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(ApiError error) {
        log.debug("Building error response: {} - {}", error.getStatus(), error.getMessage());
        return new ResponseEntity<>(new ApiResponse<>(error), error.getStatus());
    }
}
