package com.redizego.redi_ze_go.advices;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Standardized error response model for the Redizego API.
 * 
 * This class represents the structure of error responses returned by the API
 * when exceptions occur. It provides consistent error information including
 * status codes, messages, and detailed validation errors when applicable.
 * 
 * Key Features:
 * - Standardized error response structure
 * - HTTP status code integration
 * - Support for multiple error messages (validation scenarios)
 * - Builder pattern for flexible construction
 * - Integration with GlobalExceptionHandler
 * 
 * Usage Scenarios:
 * - Single error message (ResourceNotFoundException, etc.)
 * - Multiple validation errors (MethodArgumentNotValidException)
 * - Business logic violations (RuntimeConflictException)
 * - Authentication and authorization failures
 * 
 * Response Structure:
 * {
 *   "message": "Primary error message",
 *   "status": "HTTP_STATUS",
 *   "errors": ["Detailed error 1", "Detailed error 2"]
 * }
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see GlobalExceptionHandler
 * @see ApiResponse
 * 
 * @implNote Uses Lombok @Builder for convenient object creation
 * @implNote Designed to be wrapped in ApiResponse by GlobalExceptionHandler
 */
@Data
@Builder
public class ApiError {

    /**
     * Primary error message describing what went wrong.
     * 
     * This should be a user-friendly message that explains the error
     * in terms that the API consumer can understand and act upon.
     * 
     * @example "User not found with ID: 123"
     * @example "Validation failed"
     * @example "Authentication required"
     */
    private String message;

    /**
     * HTTP status code associated with this error.
     * 
     * Used by GlobalExceptionHandler to set the appropriate HTTP response
     * status code. This allows the client to programmatically determine
     * the type of error that occurred.
     * 
     * @see HttpStatus
     */
    private HttpStatus status;

    /**
     * List of detailed error messages, typically used for validation errors.
     * 
     * When a single operation fails for multiple reasons (e.g., form validation
     * with multiple field errors), this list contains all the specific error
     * messages. Can be null for simple errors with only a primary message.
     * 
     * @example ["Email is required", "Password must be at least 8 characters"]
     * @example ["Pickup location is invalid", "Destination cannot be empty"]
     */
    private List<String> errors;

}
