package com.redizego.redi_ze_go.exceptions;

/**
 * Custom runtime exception thrown when a business rule violation or conflict occurs.
 * 
 * This exception is used to signal that an operation cannot be completed due to
 * a conflict with the current state of the system or business rules. It represents
 * situations where the request is syntactically correct but cannot be processed
 * due to business logic constraints.
 * 
 * Common Use Cases:
 * - Attempting to create a user with an email that already exists
 * - Trying to onboard a user as a driver when they're already a driver
 * - Attempting to cancel a ride that's already in progress
 * - Trying to start a ride that's not in CONFIRMED status
 * - Driver trying to accept multiple rides simultaneously
 * - Rider attempting to request a new ride while having an active ride
 * 
 * HTTP Mapping:
 * This exception is typically mapped to HTTP 409 Conflict status
 * by the global exception handler.
 * 
 * Business Logic:
 * This exception should be thrown when business invariants are violated,
 * helping maintain data consistency and enforce business rules.
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see com.redizego.redi_ze_go.advices.GlobalExceptionHandler
 * @see ResourceNotFoundException
 * 
 * @implNote Extends RuntimeException to avoid checked exception handling
 * @implNote Should include descriptive message explaining the conflict
 */
public class RuntimeConflictException extends RuntimeException {
    
    /**
     * Constructs a new RuntimeConflictException with no detail message.
     * 
     * @implNote Consider using the message constructor for better error reporting
     */
    public RuntimeConflictException() {
        super();
    }

    /**
     * Constructs a new RuntimeConflictException with the specified detail message.
     * 
     * The detail message should clearly explain what business rule was violated
     * or what conflict occurred, helping users understand why their request failed.
     * 
     * @param message the detail message explaining the conflict or business rule violation
     * 
     * @example "User already exists with email: user@example.com"
     * @example "Driver is already assigned to another ride"
     * @example "Cannot cancel ride in ONGOING status"
     * @example "Rider already has an active ride request"
     */
    public RuntimeConflictException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new RuntimeConflictException with the specified detail message and cause.
     * 
     * This constructor is useful when the conflict is detected as a result of
     * another exception (e.g., unique constraint violation from database).
     * 
     * @param message the detail message explaining the conflict
     * @param cause the cause of this exception
     */
    public RuntimeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
