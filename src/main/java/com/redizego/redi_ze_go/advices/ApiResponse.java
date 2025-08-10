package com.redizego.redi_ze_go.advices;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standardized response wrapper for all Redizego API responses.
 * 
 * This generic class provides a consistent structure for all API responses,
 * whether successful or failed. It includes metadata like timestamps and
 * can carry either successful data or error information.
 * 
 * Key Features:
 * - Consistent response structure across all endpoints
 * - Automatic timestamp generation for response tracking
 * - Generic type support for any response data
 * - Separate fields for success data and error information
 * - Integration with GlobalResponseHandler and GlobalExceptionHandler
 * 
 * Response Structure:
 * Successful Response:
 * {
 *   "timestamp": "2023-10-15T14:30:00.123",
 *   "data": { ... response payload ... },
 *   "error": null
 * }
 * 
 * Error Response:
 * {
 *   "timestamp": "2023-10-15T14:30:00.123",
 *   "data": null,
 *   "error": { ... error details ... }
 * }
 * 
 * Usage:
 * - Success: new ApiResponse<>(responseData)
 * - Error: new ApiResponse<>(apiError)
 * - Empty: new ApiResponse<>()
 * 
 * @param <T> The type of the response data payload
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see GlobalResponseHandler
 * @see GlobalExceptionHandler
 * @see ApiError
 * 
 * @implNote Automatically wrapped around controller responses by GlobalResponseHandler
 * @implNote Used by GlobalExceptionHandler for error responses
 */
@Data
public class ApiResponse<T> {

    /**
     * Timestamp when the response was created.
     * 
     * Automatically set to the current system time when the ApiResponse
     * is instantiated. Useful for debugging, logging, and client-side
     * response correlation.
     * 
     * @implNote Uses LocalDateTime for consistent timezone handling
     */
    private LocalDateTime timestamp;

    /**
     * The actual response data payload.
     * 
     * Contains the successful response data when the API call completes
     * without errors. Will be null for error responses.
     * 
     * @example UserDto for user profile endpoints
     * @example Page<RideDto> for paginated ride lists
     * @example String for simple confirmation messages
     */
    private T data;

    /**
     * Error information when the API call fails.
     * 
     * Contains detailed error information when exceptions occur.
     * Will be null for successful responses.
     * 
     * @see ApiError
     */
    private ApiError error;

    /**
     * Default constructor that initializes timestamp.
     * 
     * Creates an ApiResponse with current timestamp and null data/error.
     * Typically used as a base for other constructors.
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for successful responses with data payload.
     * 
     * Creates an ApiResponse containing successful response data.
     * The error field will be null, indicating a successful operation.
     * 
     * @param data The response data payload
     * 
     * @implNote Most common constructor used by GlobalResponseHandler
     */
    public ApiResponse(T data) {
        this();
        this.data = data;
    }

    /**
     * Constructor for error responses.
     * 
     * Creates an ApiResponse containing error information.
     * The data field will be null, indicating a failed operation.
     * 
     * @param error The error details including status and messages
     * 
     * @implNote Used by GlobalExceptionHandler for exception responses
     */
    public ApiResponse(ApiError error) {
        this();
        this.error = error;
    }
}
