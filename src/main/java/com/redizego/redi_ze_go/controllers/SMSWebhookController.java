package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.PointDto;
import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.services.RiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling SMS webhook requests for offline ride booking.
 * 
 * This controller provides endpoints to receive SMS messages from external services
 * (like Twilio) and process them into ride requests. It supports offline booking
 * capabilities for users who don't have access to the mobile application.
 * 
 * Expected SMS format: "pickup: lat,lng drop: lat,lng"
 * Example: "pickup: 12.9716,77.5946 drop: 13.0827,80.2707"
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController("/sms-webhook")
@RequiredArgsConstructor
public class SMSWebhookController {

    private final BeanDefinitionRegistryPostProcessor springSecurityPathPatternParserBeanDefinitionRegistryPostProcessor;
    private final RiderService riderService;

    /**
     * Handles incoming SMS webhook requests for offline ride booking.
     * 
     * This endpoint is designed to work with SMS gateway services like Twilio.
     * It processes SMS messages containing pickup and destination coordinates
     * and converts them into ride requests.
     * 
     * @param from The phone number of the SMS sender (optional)
     * @param body The SMS message body containing pickup and drop locations
     *             Expected format: "pickup: lat,lng drop: lat,lng"
     * @return ResponseEntity containing the processed RideRequestDto
     * @throws NumberFormatException if coordinates are not in valid format
     * 
     * @apiNote This endpoint supports offline booking for users without app access
     */
    @PostMapping("/request-offline-ride")
    public ResponseEntity<RideRequestDto> requestOfflineRide(
            @RequestParam(value = "From", required = false) String from,
            @RequestParam(value = "Body", required = false) String body) {
        
        log.info("Received SMS webhook request from: {} with body: {}", from, body);
        
        try {
            // Create new ride request DTO
            RideRequestDto rideRequestDto = new RideRequestDto();
            
            // Parse and set pickup location
            double[] pickupCoordinates = parseCoordinates(parsePickup(body));
            rideRequestDto.setPickupLocation(new PointDto(pickupCoordinates));
            
            // Parse and set destination location
            double[] dropCoordinates = parseCoordinates(parseDrop(body));
            rideRequestDto.setDestinationLocation(new PointDto(dropCoordinates));
            
            log.debug("Parsed coordinates - Pickup: [{}, {}], Drop: [{}, {}]", 
                    pickupCoordinates[0], pickupCoordinates[1],
                    dropCoordinates[0], dropCoordinates[1]);
            
            // Process the offline ride request
            RideRequestDto processedRequest = riderService.handleOfflineRideRequest(rideRequestDto);
            
            log.info("Successfully processed offline ride request with ID: {}", 
                    processedRequest.getId());
            
            return ResponseEntity.ok().body(processedRequest);
            
        } catch (Exception e) {
            log.error("Error processing SMS webhook request: {}", e.getMessage(), e);
            throw e; // Let global exception handler manage the response
        }
    }

    /**
     * Extracts pickup location from SMS message body.
     * 
     * Searches for "pickup:" keyword and extracts the text between
     * "pickup:" and "drop:" (or end of message if no "drop:" found).
     * 
     * @param body The SMS message body
     * @return The pickup location string, empty if not found
     * 
     * @example Input: "pickup: 12.9716,77.5946 drop: 13.0827,80.2707"
     *          Output: "12.9716,77.5946"
     */
    private String parsePickup(String body) {
        if (body == null || body.trim().isEmpty()) {
            log.warn("Empty SMS body provided for pickup parsing");
            return "";
        }
        
        int start = body.toLowerCase().indexOf("pickup:");
        if (start == -1) {
            log.warn("No 'pickup:' keyword found in SMS body: {}", body);
            return "";
        }
        
        int end = body.toLowerCase().indexOf("drop:");
        String pickup = body.substring(start + 7, (end == -1 ? body.length() : end)).trim();
        
        log.debug("Parsed pickup location: {}", pickup);
        return pickup;
    }

    /**
     * Extracts destination location from SMS message body.
     * 
     * Searches for "drop:" keyword and extracts the text from
     * "drop:" to the end of the message.
     * 
     * @param body The SMS message body
     * @return The destination location string, empty if not found
     * 
     * @example Input: "pickup: 12.9716,77.5946 drop: 13.0827,80.2707"
     *          Output: "13.0827,80.2707"
     */
    private String parseDrop(String body) {
        if (body == null || body.trim().isEmpty()) {
            log.warn("Empty SMS body provided for drop parsing");
            return "";
        }
        
        int start = body.toLowerCase().indexOf("drop:");
        if (start == -1) {
            log.warn("No 'drop:' keyword found in SMS body: {}", body);
            return "";
        }
        
        String drop = body.substring(start + 5).trim();
        log.debug("Parsed drop location: {}", drop);
        return drop;
    }

    /**
     * Parses coordinate string into latitude and longitude array.
     * 
     * Expects location in "latitude,longitude" format and converts
     * it to a double array with [latitude, longitude].
     * 
     * @param location The location string in "lat,lng" format
     * @return Array of [latitude, longitude] or [0.0, 0.0] if parsing fails
     * @throws NumberFormatException if coordinates are not valid numbers
     * 
     * @example Input: "12.9716,77.5946"
     *          Output: [12.9716, 77.5946]
     */
    private double[] parseCoordinates(String location) {
        if (location == null || location.trim().isEmpty()) {
            log.warn("Empty location string provided for coordinate parsing");
            return new double[]{0.0, 0.0};
        }
        
        try {
            String[] parts = location.split(",");
            if (parts.length != 2) {
                log.warn("Invalid coordinate format: {}. Expected 'lat,lng'", location);
                return new double[]{0.0, 0.0};
            }
            
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());
            
            // Basic validation for coordinate ranges
            if (latitude < -90 || latitude > 90) {
                log.warn("Invalid latitude: {}. Must be between -90 and 90", latitude);
                return new double[]{0.0, 0.0};
            }
            
            if (longitude < -180 || longitude > 180) {
                log.warn("Invalid longitude: {}. Must be between -180 and 180", longitude);
                return new double[]{0.0, 0.0};
            }
            
            log.debug("Successfully parsed coordinates: [{}, {}]", latitude, longitude);
            return new double[]{latitude, longitude};
            
        } catch (NumberFormatException e) {
            log.error("Failed to parse coordinates from: {}", location, e);
            return new double[]{0.0, 0.0};
        }
    }

}
