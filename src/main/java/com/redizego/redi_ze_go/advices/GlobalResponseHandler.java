package com.redizego.redi_ze_go.advices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Global response handler that wraps all REST API responses in a standardized format.
 * 
 * This class implements ResponseBodyAdvice to intercept and modify response bodies
 * before they are written to the HTTP response. It ensures all API responses follow
 * a consistent structure by wrapping them in ApiResponse objects.
 * 
 * Key Features:
 * - Standardized response format across all endpoints
 * - Automatic timestamp addition to responses
 * - Exclusion of documentation and monitoring endpoints
 * - Preserves error responses from GlobalExceptionHandler
 * 
 * Response Structure:
 * {
 *   "timestamp": "2023-10-15T14:30:00",
 *   "data": { ... actual response data ... },
 *   "error": null
 * }
 * 
 * Excluded Paths:
 * - /v3/api-docs* (OpenAPI documentation)
 * - /actuator/* (Spring Boot Actuator endpoints)
 * 
 * Error Handling:
 * - ApiError responses are not wrapped (already handled by GlobalExceptionHandler)
 * - Allows proper error response format to be maintained
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see ApiResponse
 * @see ApiError
 * @see GlobalExceptionHandler
 * 
 * @implNote Applied globally to all REST controllers via @RestControllerAdvice
 * @implNote Processing order: Controller -> GlobalResponseHandler -> GlobalExceptionHandler (if error)
 */
@Slf4j
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    /**
     * Determines whether this advice should be applied to the response.
     * 
     * This method is called for every controller method response to determine
     * if the response should be processed by this handler. Currently returns
     * true for all responses, but specific filtering is done in beforeBodyWrite.
     * 
     * @param returnType The return type of the controller method
     * @param converterType The message converter class being used
     * @return true if this handler should process the response, false otherwise
     * 
     * @implNote Could be optimized to exclude certain response types here
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to all controller responses - specific filtering done in beforeBodyWrite
        return true;
    }

    /**
     * Intercepts and modifies the response body before it's written to HTTP response.
     * 
     * This method wraps successful responses in ApiResponse objects while excluding
     * documentation endpoints, actuator endpoints, and error responses.
     * 
     * Processing Logic:
     * 1. Skip OpenAPI documentation endpoints (/v3/api-docs)
     * 2. Skip Spring Actuator monitoring endpoints (/actuator)
     * 3. Skip already processed error responses (ApiError instances)
     * 4. Wrap remaining responses in ApiResponse with timestamp
     * 
     * @param body The response body to be written
     * @param returnType The return type of the controller method
     * @param selectedContentType The content type selected for the response
     * @param selectedConverterType The converter selected to write to the response
     * @param request The current HTTP request
     * @param response The current HTTP response
     * @return The modified response body (wrapped in ApiResponse or original)
     * 
     * @implNote Preserves original response for excluded paths and error responses
     */
    @Override
    public Object beforeBodyWrite(
            Object body, 
            MethodParameter returnType, 
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, 
            ServerHttpRequest request, 
            ServerHttpResponse response) {

        String requestPath = request.getURI().getPath();
        
        // Skip OpenAPI documentation endpoints
        if (requestPath.contains("/v3/api-docs")) {
            log.debug("Skipping response wrapping for OpenAPI documentation: {}", requestPath);
            return body;
        }

        // Skip Spring Boot Actuator endpoints (health checks, metrics, etc.)
        if (requestPath.contains("/actuator")) {
            log.debug("Skipping response wrapping for Actuator endpoint: {}", requestPath);
            return body;
        }

        // Skip already processed error responses from GlobalExceptionHandler
        if (body instanceof ApiError) {
            log.debug("Skipping response wrapping for ApiError: {}", requestPath);
            return body;
        }

        // Skip if response is already wrapped in ApiResponse
        if (body instanceof ApiResponse) {
            log.debug("Response already wrapped in ApiResponse: {}", requestPath);
            return body;
        }

        // Wrap successful responses in standardized ApiResponse format
        log.debug("Wrapping response in ApiResponse for endpoint: {}", requestPath);
        return new ApiResponse<>(body);
    }
}
