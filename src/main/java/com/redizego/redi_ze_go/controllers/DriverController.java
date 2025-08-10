package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.services.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling driver-specific operations in the cab booking system.
 * 
 * This controller provides endpoints for drivers to:
 * - Accept incoming ride requests
 * - Start rides with OTP verification
 * - End completed rides
 * - Cancel rides when necessary
 * - View their profile and earnings
 * - Retrieve ride history with pagination
 * - Rate riders after completed rides
 * 
 * Security:
 * - All endpoints require ROLE_DRIVER authorization
 * - JWT authentication is enforced through Spring Security
 * - Current driver context is automatically injected via SecurityContextHolder
 * 
 * Key Features:
 * - OTP-based ride verification system
 * - Real-time ride status management
 * - Rider rating system integration
 * - Comprehensive ride history tracking
 * - Driver availability management
 * 
 * Business Rules:
 * - Drivers can only accept rides when available
 * - OTP verification required to start rides
 * - Drivers must end rides to accept new ones
 * - Rating system affects driver visibility in matching
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see DriverService
 * @see com.redizego.redi_ze_go.entities.Driver
 * @see com.redizego.redi_ze_go.security.JwtAuthFilter
 */
@Slf4j
@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
@Secured("ROLE_DRIVER")
public class DriverController {

    private final DriverService driverService;

    /**
     * Allows authenticated driver to accept a pending ride request.
     * 
     * This endpoint enables drivers to accept ride requests that have been
     * matched to them by the driver matching strategy. Once accepted, the
     * ride status changes to CONFIRMED and an OTP is generated for verification.
     * 
     * @param rideRequestId The unique identifier of the ride request to accept
     * @return ResponseEntity with RideDto containing ride details and OTP
//     * @throws ResourceNotFoundException if ride request doesn't exist
//     * @throws RuntimeConflictException if driver is not available or ride already accepted
//     * @throws UnauthorizedException if driver is not eligible for this ride
     * 
     * @implNote Driver availability status is automatically updated upon acceptance
     * @implNote OTP is generated and sent to rider for verification
     */
    @PostMapping("/accept-ride/{ride-request-id}")
    public ResponseEntity<RideDto> acceptRide(@PathVariable("ride-request-id") Long rideRequestId) {
        log.info("Ride acceptance request for ride request ID: {}", rideRequestId);
        
        try {
            RideDto acceptedRide = driverService.acceptRide(rideRequestId);
            
            log.info("Successfully accepted ride request ID: {}, ride ID: {}, OTP: {}", 
                    rideRequestId, acceptedRide.getId(), acceptedRide.getOtp());
            
            return ResponseEntity.ok(acceptedRide);
            
        } catch (Exception e) {
            log.error("Failed to accept ride request ID: {}", rideRequestId, e);
            throw e;
        }
    }

    /**
     * Starts an accepted ride with OTP verification.
     * 
     * This endpoint allows drivers to start a ride after the rider provides
     * the correct OTP. The OTP verification ensures the right rider is picked up.
     * Once verified, the ride status changes to ONGOING.
     * 
     * @param rideId The unique identifier of the ride to start
     * @param rideStartDto Contains the OTP provided by the rider
     * @return ResponseEntity with updated RideDto showing ONGOING status
//     * @throws ResourceNotFoundException if ride doesn't exist
//     * @throws RuntimeConflictException if ride is not in CONFIRMED status
//     * @throws IllegalArgumentException if OTP doesn't match
     * 
     * @implNote Timer starts for fare calculation once ride begins
     * @implNote Driver location tracking may be enabled during ride
     */
    @PostMapping("/start-ride/{ride-id}")
    public ResponseEntity<RideDto> startRide(
            @PathVariable("ride-id") Long rideId,
            @RequestBody RideStartDto rideStartDto) {
        
        log.info("Ride start request for ride ID: {} with OTP verification", rideId);
        
        try {
            RideDto startedRide = driverService.startRide(rideId, rideStartDto.getOtp());
            
            log.info("Successfully started ride ID: {} with status: {}", 
                    rideId, startedRide.getRideStatus());
            
            return ResponseEntity.ok(startedRide);
            
        } catch (Exception e) {
            log.error("Failed to start ride ID: {}", rideId, e);
            throw e;
        }
    }

    /**
     * Ends an ongoing ride and processes payment.
     * 
     * This endpoint allows drivers to complete rides when they reach the
     * destination. It calculates final fare, processes payment, and updates
     * driver availability status.
     * 
     * @param rideId The unique identifier of the ride to end
     * @return ResponseEntity with completed RideDto including final fare
//     * @throws ResourceNotFoundException if ride doesn't exist
//     * @throws RuntimeConflictException if ride is not in ONGOING status
     * 
     * @implNote Final fare calculation includes distance, time, and surge pricing
     * @implNote Payment is processed through configured payment strategy
     * @implNote Driver becomes available for new rides after completion
     */
    @PostMapping("/end-ride/{ride-id}")
    public ResponseEntity<RideDto> endRide(@PathVariable("ride-id") Long rideId) {
        log.info("Ride completion request for ride ID: {}", rideId);
        
        try {
            RideDto completedRide = driverService.endRide(rideId);
            
            log.info("Successfully completed ride ID: {} with final fare: {} and status: {}", 
                    rideId, completedRide.getFare(), completedRide.getRideStatus());
            
            return ResponseEntity.ok(completedRide);
            
        } catch (Exception e) {
            log.error("Failed to end ride ID: {}", rideId, e);
            throw e;
        }
    }

    /**
     * Cancels a ride from the driver's side.
     * 
     * This endpoint allows drivers to cancel rides in CONFIRMED status
     * due to various reasons (emergency, vehicle issues, etc.).
     * Frequent cancellations may affect driver rating and availability.
     * 
     * @param rideId The unique identifier of the ride to cancel
     * @return ResponseEntity with cancelled RideDto
//     * @throws ResourceNotFoundException if ride doesn't exist
//     * @throws RuntimeConflictException if ride cannot be cancelled
//     * @throws UnauthorizedException if current driver doesn't own the ride
     * 
     * @implNote Driver becomes available for new rides after cancellation
     * @implNote Cancellation may trigger retry logic for failed ride requests
     */
    @PostMapping("/cancel-ride/{ride-id}")
    public ResponseEntity<RideDto> cancelRide(@PathVariable("ride-id") Long rideId) {
        log.info("Ride cancellation request from driver for ride ID: {}", rideId);
        
        try {
            RideDto cancelledRide = driverService.cancelRide(rideId);
            
            log.info("Successfully cancelled ride ID: {} with status: {}", 
                    rideId, cancelledRide.getRideStatus());
            
            return ResponseEntity.ok().body(cancelledRide);
            
        } catch (Exception e) {
            log.error("Failed to cancel ride ID: {}", rideId, e);
            throw e;
        }
    }

    /**
     * Retrieves the authenticated driver's profile information.
     * 
     * Returns comprehensive driver profile including:
     * - Basic information (name, vehicle details, license)
     * - Average rating received from riders
     * - Total rides completed and earnings
     * - Current availability status
     * - Performance metrics
     * 
     * @return ResponseEntity with DriverDto containing profile information
//     * @throws ResourceNotFoundException if driver profile not found
     * 
     * @implNote Profile is retrieved based on JWT token's user ID
     */
    @GetMapping("/my-profile")
    public ResponseEntity<DriverDto> getMyProfile() {
        log.debug("Profile request received from authenticated driver");
        
        try {
            DriverDto driverProfile = driverService.getMyProfile();
            
            log.debug("Successfully retrieved profile for driver ID: {}", driverProfile.getId());
            
            return ResponseEntity.ok().body(driverProfile);
            
        } catch (Exception e) {
            log.error("Failed to retrieve driver profile: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves paginated ride history for the authenticated driver.
     * 
     * This endpoint returns all rides associated with the current driver,
     * sorted by creation time (newest first) with pagination support.
     * Useful for tracking earnings, performance, and ride patterns.
     * 
     * @param pageOffset Zero-based page number (default: 0)
     * @param pageSize Number of rides per page (default: 10, max recommended: 50)
     * @return ResponseEntity with Page of RideDto containing ride history
     * 
     * @implNote Results are sorted by createdTime DESC, then by ID DESC
     * @implNote Includes rides in all statuses (CONFIRMED, ONGOING, COMPLETED, CANCELLED)
     */
    @GetMapping("/all-rides")
    public ResponseEntity<Page<RideDto>> getAllRides(
            @RequestParam(defaultValue = "0") Integer pageOffset,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.debug("Ride history request from driver - page: {}, size: {}", pageOffset, pageSize);
        
        try {
            // Create pageable with descending sort by creation time and ID
            PageRequest pageRequest = PageRequest.of(
                    pageOffset, 
                    pageSize, 
                    Sort.by(Sort.Direction.DESC, "createdTime", "id")
            );
            
            Page<RideDto> rideHistory = driverService.getAllRides(pageRequest);
            
            log.info("Retrieved {} rides for driver (page {}/{})", 
                    rideHistory.getNumberOfElements(), 
                    rideHistory.getNumber() + 1, 
                    rideHistory.getTotalPages());
            
            return ResponseEntity.ok().body(rideHistory);
            
        } catch (Exception e) {
            log.error("Failed to retrieve driver ride history: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Allows driver to rate a rider after completing a ride.
     * 
     * This endpoint enables drivers to provide feedback on their experience
     * with riders. Ratings help maintain platform quality and rider behavior.
     * 
     * @param rideId The unique identifier of the completed ride
     * @param rating Rider rating from 1 (poor) to 5 (excellent)
     * @return ResponseEntity with updated RiderDto including new rating
//     * @throws ResourceNotFoundException if ride doesn't exist or driver doesn't own it
//     * @throws RuntimeConflictException if ride is not in COMPLETED status
//     * @throws IllegalArgumentException if rating is not between 1 and 5
     * 
     * @implNote Drivers can only rate riders once per ride
     * @implNote Rating updates rider's overall rating using weighted average
     */
    @PostMapping("/rate-rider/{ride-id}/{rating}")
    public ResponseEntity<RiderDto> rateRider(
            @PathVariable("ride-id") Long rideId,
            @PathVariable("rating") Integer rating) {
        
        log.info("Rider rating request from driver - ride ID: {}, rating: {}", rideId, rating);
        
        // Validate rating range
        if (rating < 1 || rating > 5) {
            log.warn("Invalid rating value: {}. Must be between 1 and 5", rating);
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        try {
            RiderDto ratedRider = driverService.rateRider(rideId, rating);
            
            log.info("Successfully rated rider ID: {} with rating: {} for ride ID: {}", 
                    ratedRider.getId(), rating, rideId);
            
            return ResponseEntity.ok().body(ratedRider);
            
        } catch (Exception e) {
            log.error("Failed to rate rider for ride ID: {}", rideId, e);
            throw e;
        }
    }
}
