package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.services.RiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling rider-specific operations in the cab booking system.
 * 
 * This controller provides endpoints for riders to:
 * - Request new rides
 * - Cancel existing rides
 * - View their profile information
 * - Retrieve ride history with pagination
 * - Rate drivers after completed rides
 * 
 * Security:
 * - All endpoints require ROLE_RIDER authorization
 * - JWT authentication is enforced through Spring Security
 * - Current rider context is automatically injected via SecurityContextHolder
 * 
 * Key Features:
 * - Paginated ride history retrieval
 * - Real-time ride request processing
 * - Driver rating system integration
 * - Comprehensive error handling
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see RiderService
 * @see com.redizego.redi_ze_go.entities.Rider
 * @see com.redizego.redi_ze_go.security.JwtAuthFilter
 */
@Slf4j
@RestController
@RequestMapping("/rider")
@RequiredArgsConstructor
@Secured("ROLE_RIDER")
public class RiderController {

    private final RiderService riderService;

    /**
     * Creates a new ride request for the authenticated rider.
     * 
     * This endpoint processes ride requests by:
     * 1. Validating pickup and destination locations
     * 2. Calculating estimated fare using strategy pattern
     * 3. Finding matching drivers using configured matching strategy
     * 4. Creating ride request entity and notifying available drivers
     * 
     * @param rideRequestDto Contains pickup location, destination, payment method, etc.
     * @return ResponseEntity with created RideRequestDto including estimated fare and ETA
     * @throws IllegalArgumentException if pickup/destination coordinates are invalid
//     * @throws RuntimeConflictException if rider has active ride request
     * 
     * @apiNote Supports both online app requests and offline SMS-based requests
     */
    @PostMapping("/request-ride")
    public ResponseEntity<RideRequestDto> requestRide(@RequestBody RideRequestDto rideRequestDto) {
        log.info("Ride request received from rider for pickup location: [{}, {}]", 
                rideRequestDto.getPickupLocation().getCoordinates()[0],
                rideRequestDto.getPickupLocation().getCoordinates()[1]);
        
        try {
            RideRequestDto processedRequest = riderService.requestRide(rideRequestDto);
            
            log.info("Successfully created ride request with ID: {} and estimated fare: {}", 
                    processedRequest.getId(), processedRequest.getFare());
            
            return ResponseEntity.ok().body(processedRequest);
            
        } catch (Exception e) {
            log.error("Failed to create ride request: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cancels an existing ride for the authenticated rider.
     * 
     * This endpoint allows riders to cancel rides in REQUESTED or CONFIRMED status.
     * Cancellation may incur charges based on timing and ride status.
     * 
     * @param rideId The unique identifier of the ride to cancel
     * @return ResponseEntity with updated RideDto showing CANCELLED status
//     * @throws ResourceNotFoundException if ride with given ID doesn't exist
//     * @throws RuntimeConflictException if ride cannot be cancelled (e.g., already ONGOING)
//     * @throws UnauthorizedException if current rider doesn't own the ride
     * 
     * @implNote Cancellation fees may apply based on ride status and timing
     */
    @PostMapping("/cancel-ride/{ride-id}")
    public ResponseEntity<RideDto> cancelRide(@PathVariable("ride-id") Long rideId) {
        log.info("Ride cancellation request for ride ID: {}", rideId);
        
        try {
            RideDto cancelledRide = riderService.cancelRide(rideId);
            
            log.info("Successfully cancelled ride ID: {} with status: {}", 
                    rideId, cancelledRide.getRideStatus());
            
            return ResponseEntity.ok().body(cancelledRide);
            
        } catch (Exception e) {
            log.error("Failed to cancel ride ID: {}", rideId, e);
            throw e;
        }
    }

    /**
     * Retrieves the authenticated rider's profile information.
     * 
     * Returns comprehensive rider profile including:
     * - Basic information (name, email, phone)
     * - Average rating received from drivers
     * - Total rides completed
     * - Account status and preferences
     * 
     * @return ResponseEntity with RiderDto containing profile information
//     * @throws ResourceNotFoundException if rider profile not found
     * 
     * @implNote Profile is retrieved based on JWT token's user ID
     */
    @GetMapping("/my-profile")
    public ResponseEntity<RiderDto> getMyProfile() {
        log.debug("Profile request received from authenticated rider");
        
        try {
            RiderDto riderProfile = riderService.getMyProfile();
            
            log.debug("Successfully retrieved profile for rider ID: {}", riderProfile.getId());
            
            return ResponseEntity.ok().body(riderProfile);
            
        } catch (Exception e) {
            log.error("Failed to retrieve rider profile: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves paginated ride history for the authenticated rider.
     * 
     * This endpoint returns all rides associated with the current rider,
     * sorted by creation time (newest first) with pagination support.
     * 
     * @param pageOffset Zero-based page number (default: 0)
     * @param pageSize Number of rides per page (default: 10, max recommended: 50)
     * @return ResponseEntity with Page of RideDto containing ride history
     * 
     * @implNote Results are sorted by createdTime DESC, then by ID DESC
     * @implNote Includes rides in all statuses (REQUESTED, ONGOING, COMPLETED, CANCELLED)
     */
    @GetMapping("/all-rides")
    public ResponseEntity<Page<RideDto>> getAllRides(
            @RequestParam(defaultValue = "0") Integer pageOffset,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.debug("Ride history request - page: {}, size: {}", pageOffset, pageSize);
        
        try {
            // Create pageable with descending sort by creation time and ID
            PageRequest pageRequest = PageRequest.of(
                    pageOffset, 
                    pageSize, 
                    Sort.by(Sort.Direction.DESC, "createdTime", "id")
            );
            
            Page<RideDto> rideHistory = riderService.getAllRides(pageRequest);
            
            log.info("Retrieved {} rides for rider (page {}/{})", 
                    rideHistory.getNumberOfElements(), 
                    rideHistory.getNumber() + 1, 
                    rideHistory.getTotalPages());
            
            return ResponseEntity.ok().body(rideHistory);
            
        } catch (Exception e) {
            log.error("Failed to retrieve ride history: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Allows rider to rate a driver after completing a ride.
     * 
     * This endpoint enables riders to provide feedback on their ride experience
     * by rating the driver. Ratings contribute to the driver's overall rating score.
     * 
     * @param rideId The unique identifier of the completed ride
     * @param rating Driver rating from 1 (poor) to 5 (excellent)
     * @return ResponseEntity with updated DriverDto including new rating
//     * @throws ResourceNotFoundException if ride doesn't exist or rider doesn't own it
//     * @throws RuntimeConflictException if ride is not in COMPLETED status
//     * @throws IllegalArgumentException if rating is not between 1 and 5
     * 
     * @implNote Riders can only rate drivers once per ride
     * @implNote Rating updates driver's overall rating using weighted average
     */
    @PostMapping("/rate-driver/{ride-id}/{rating}")
    public ResponseEntity<DriverDto> rateDriver(
            @PathVariable("ride-id") Long rideId,
            @PathVariable("rating") Integer rating) {
        
        log.info("Driver rating request - ride ID: {}, rating: {}", rideId, rating);
        
        // Validate rating range
        if (rating < 1 || rating > 5) {
            log.warn("Invalid rating value: {}. Must be between 1 and 5", rating);
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        try {
            DriverDto ratedDriver = riderService.rateDriver(rideId, rating);
            
            log.info("Successfully rated driver ID: {} with rating: {} for ride ID: {}", 
                    ratedDriver.getId(), rating, rideId);
            
            return ResponseEntity.ok().body(ratedDriver);
            
        } catch (Exception e) {
            log.error("Failed to rate driver for ride ID: {}", rideId, e);
            throw e;
        }
    }
}
