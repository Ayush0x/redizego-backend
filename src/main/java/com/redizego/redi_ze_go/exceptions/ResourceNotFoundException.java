package com.redizego.redi_ze_go.exceptions;

/**
 * Custom runtime exception thrown when a requested resource is not found.
 * 
 * This exception is typically thrown by service layer methods when attempting
 * to retrieve entities that don't exist in the database. It extends RuntimeException
 * to avoid forced exception handling in client code, following Spring's philosophy
 * of using unchecked exceptions for business logic violations.
 * 
 * Common Use Cases:
 * - User not found by ID during authentication
 * - Ride request not found when accepting/canceling
 * - Driver not found during matching process
 * - Wallet not found during payment processing
 * 
 * HTTP Mapping:
 * This exception is typically mapped to HTTP 404 Not Found status
 * by the global exception handler.
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see com.redizego.redi_ze_go.advices.GlobalExceptionHandler
 * @see RuntimeConflictException
 * 
 * @implNote Extends RuntimeException to avoid checked exception handling
 * @implNote Should include descriptive message indicating what resource was not found
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new ResourceNotFoundException with no detail message.
     * 
     * @implNote Consider using the message constructor for better error reporting
     */
    public ResourceNotFoundException() {
        super();
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * 
     * The detail message should clearly indicate what resource was not found
     * and potentially include the identifier that was used in the search.
     * 
     * @param message the detail message explaining what resource was not found
     * 
     * @example "User not found with ID: 123"
     * @example "Ride request not found with ID: 456"
     * @example "Driver not found for user ID: 789"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     * 
     * This constructor is useful when the ResourceNotFoundException is caused by
     * another exception (e.g., database access exception).
     * 
     * @param message the detail message explaining what resource was not found
     * @param cause the cause of this exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
