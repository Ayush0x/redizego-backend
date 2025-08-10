package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.repositories.DriverRepository;
import com.redizego.redi_ze_go.services.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementation for driver-related business operations.
 * 
 * This service manages the complete driver workflow including ride acceptance,
 * ride lifecycle management (start, end, cancel), rider rating, profile management,
 * and ride history. The service ensures proper business rule enforcement
 * throughout all driver operations.
 * 
 * Key Features:
 * - Ride request acceptance with availability validation
 * - Complete ride lifecycle management (confirm → start → end)
 * - OTP-based ride start verification for security
 * - Automatic payment and rating initialization
 * - Driver availability management
 * - Rider rating system with ride completion verification
 * - Paginated ride history retrieval
 * - Profile management and authentication
 * 
 * Business Rules:
 * - Only available drivers can accept rides
 * - Rides must follow proper status transitions
 * - OTP verification required for ride start
 * - Payment processing triggered on ride end
 * - Only completed rides can be rated
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    /** Service for ride request management and validation */
    private final RideRequestService rideRequestService;
    
    /** Repository for driver data access and persistence */
    private final DriverRepository driverRepository;
    
    /** Service for ride lifecycle management */
    private final RideService rideService;
    
    /** ModelMapper for entity-DTO conversions */
    private final ModelMapper modelMapper;
    
    /** Service for payment processing and management */
    private final PaymentService paymentService;
    
    /** Service for rating management and calculations */
    private final RatingService ratingService;

    /**
     * Cancels a confirmed ride with comprehensive validation and cleanup.
     * 
     * This method allows drivers to cancel rides they have accepted but not yet started.
     * The cancellation process includes:
     * 1. Ownership verification (only the assigned driver can cancel)
     * 2. Status validation (only CONFIRMED rides can be cancelled)
     * 3. Status update to CANCELLED
     * 4. Driver availability restoration for new ride requests
     * 
     * @param rideId The unique identifier of the ride to cancel
     * @return RideDto The cancelled ride details with updated status
     * @throws RuntimeException if driver doesn't own the ride or ride status is invalid
     */
    @Override
    public RideDto cancelRide(Long rideId) {
        Ride ride=rideService.getRideById(rideId);
        Driver driver=getCurrentDriver();

        if(!driver.equals(ride.getDriver())){
            throw new RuntimeException("Driver cannot cancel this ride as he has not accepted the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.CONFIRMED)){
            throw new RuntimeException("Ride cannot be cancelled as the ride status is not invalid "+ride.getRideStatus());
        }

        rideService.updateRideStatus(ride,RideStatus.CANCELLED);
        updateDriverAvailability(driver,true);

        return modelMapper.map(ride,RideDto.class);
    }

    /**
     * Starts a confirmed ride with OTP verification and system initialization.
     * 
     * This method transitions a confirmed ride to active status with comprehensive
     * security and validation checks. The start process includes:
     * 1. Driver ownership verification
     * 2. Ride status validation (must be CONFIRMED)
     * 3. OTP verification for security
     * 4. Start time recording
     * 5. Status transition to ONGOING
     * 6. Payment record initialization
     * 7. Rating record preparation
     * 
     * The OTP verification ensures that the driver has physically met
     * the rider before starting the trip, providing security and preventing
     * fraudulent ride starts.
     * 
     * @param rideId The unique identifier of the ride to start
     * @param otp The one-time password provided by the rider for verification
     * @return RideDto The started ride with updated status and start time
     * @throws RuntimeException if driver doesn't own ride, status is invalid, or OTP is incorrect
     */
    @Override
    public RideDto startRide(Long rideId,String otp) {
        Ride ride=rideService.getRideById(rideId);
        Driver driver=getCurrentDriver();

        if(!driver.equals(ride.getDriver())){
            throw new RuntimeException("Driver cannot start this ride as he has not accepted the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.CONFIRMED)){
            throw new RuntimeException("Driver cannot start this ride as the ride status is not CONFIRMED "+ride.getRideStatus());
        }

        if(!otp.equals(ride.getOtp())){
            throw new RuntimeException("Driver cannot start this ride as the otp is not valid");
        }

        ride.setStartTime(LocalDateTime.now());
        Ride updatedRide=rideService.updateRideStatus(ride,RideStatus.ONGOING);

        paymentService.createNewPayment(updatedRide);
        ratingService.createNewRating(updatedRide);

        return modelMapper.map(updatedRide,RideDto.class);
    }

    /**
     * Completes an ongoing ride with payment processing and cleanup.
     * 
     * This method finalizes a ride that is in progress, handling all necessary
     * business logic for ride completion:
     * 1. Driver ownership verification
     * 2. Status validation (must be ONGOING)
     * 3. End time recording
     * 4. Status transition to ENDED
     * 5. Driver availability restoration
     * 6. Payment processing initiation
     * 
     * The method is transactional to ensure data consistency across
     * multiple operations (status update, availability change, payment processing).
     * 
     * @param rideId The unique identifier of the ride to complete
     * @return RideDto The completed ride with updated status and end time
     * @throws RuntimeException if driver doesn't own the ride or ride is not in progress
     */
    @Override
    @Transactional
    public RideDto endRide(Long rideId) {
        Ride ride=rideService.getRideById(rideId);
        Driver driver=getCurrentDriver();

        if(!driver.equals(ride.getDriver())){
            throw new RuntimeException("Driver cannot end this ride as he has not accepted the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.ONGOING)){
            throw new RuntimeException("Driver cannot end this ride as the ride status is not ONGOING "+ride.getRideStatus());
        }

        ride.setEndTime(LocalDateTime.now());
        rideService.updateRideStatus(ride,RideStatus.ENDED);
        updateDriverAvailability(driver,true);

        paymentService.processPayment(ride);

        return modelMapper.map(ride,RideDto.class);
    }

    /**
     * Submits a rating for the rider after ride completion.
     * 
     * This method allows drivers to rate their rider's behavior and
     * interaction during the completed ride. The rating system ensures:
     * 1. Only the assigned driver can rate the rider
     * 2. Ratings can only be submitted for ENDED rides
     * 3. Ratings contribute to rider's overall rating calculation
     * 
     * The rating affects the rider's profile and may influence future
     * driver matching algorithms and service quality.
     * 
     * @param riderId The unique identifier of the ride (note: parameter name suggests rider ID but implementation uses ride ID)
     * @param rating The rating value to assign to the rider
     * @return RiderDto The updated rider information including new rating
     * @throws RuntimeException if driver doesn't own the ride or ride is not completed
     */
    @Override
    public RiderDto rateRider(Long riderId, Integer rating) {
        Ride ride=rideService.getRideById(riderId);

        Driver driver=getCurrentDriver();

        if(!driver.equals(ride.getDriver())){
            throw new RuntimeException("Driver cannot rate this rider as he has not accepted the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.ENDED)){
            throw new RuntimeException("Driver cannot rate this rider as the ride status is not ENDED "+ride.getRideStatus());
        }

        return ratingService.rateRider(ride,rating);
    }

    /**
     * Accepts a pending ride request and creates a confirmed ride.
     * 
     * This method processes driver's acceptance of a ride request with
     * comprehensive validation and state management:
     * 1. Request status validation (must be PENDING)
     * 2. Driver availability verification
     * 3. Driver availability update (marks as unavailable)
     * 4. Ride creation with CONFIRMED status
     * 5. OTP generation for ride start verification
     * 
     * The method is transactional to ensure atomicity of the acceptance
     * process, preventing race conditions between multiple drivers
     * trying to accept the same request.
     * 
     * @param rideId The unique identifier of the ride request to accept
     * @return RideDto The newly created confirmed ride
     * @throws RuntimeException if ride request is not pending or driver is unavailable
     */
    @Override
    @Transactional
    public RideDto acceptRide(Long rideId) {
        RideRequest rideRequest=rideRequestService.findRideRequestById(rideId);

        if(!rideRequest.getRideRequestStatus().equals(RideRequestStatus.PENDING)){
            throw new RuntimeException("Ride request is cannot be accepted, statue is "+rideRequest.getRideRequestStatus());
        }

        Driver driver=getCurrentDriver();

        if(!driver.getIsAvailable()){
            throw  new RuntimeException("Driver is not available");
        }

        Driver savedDriver=updateDriverAvailability(driver,false);
        Ride ride=rideService.createNewRide(rideRequest,savedDriver);

        return modelMapper.map(ride,RideDto.class);
    }

    /**
     * Retrieves the current authenticated driver's profile information.
     * 
     * This method returns the complete driver profile including personal details,
     * rating, vehicle information, availability status, and other relevant
     * information based on the currently authenticated user context.
     * 
     * @return DriverDto The current driver's profile data
     * @throws RuntimeException if the authenticated user is not found as a driver
     */
    @Override
    public DriverDto getMyProfile() {
        Driver driver=getCurrentDriver();

        return modelMapper.map(driver,DriverDto.class);
    }

    /**
     * Retrieves a paginated list of all rides for the current driver.
     * 
     * This method provides access to the driver's complete ride history
     * with pagination support for efficient data loading and display.
     * The results include all ride statuses (completed, cancelled, ongoing, etc.)
     * and are typically sorted by most recent first.
     * 
     * @param pageRequest Pagination parameters (page number, size, sorting)
     * @return Page<RideDto> Paginated collection of the driver's rides
     * @throws RuntimeException if the authenticated user is not found as a driver
     */
    @Override
    public Page<RideDto> getAllRides(PageRequest pageRequest) {
        Driver driver=getCurrentDriver();

        return rideService.getAllRidesOfDriver(driver,pageRequest).map(
                ride->modelMapper.map(ride,RideDto.class)
        );
    }

    /**
     * Retrieves the currently authenticated driver from the security context.
     * 
     * This method extracts the authenticated user from Spring Security's
     * SecurityContext and fetches the corresponding driver profile.
     * Used throughout the service to ensure operations are performed
     * by the correct driver and for authorization checks.
     * 
     * @return Driver The authenticated driver entity
     * @throws RuntimeException if the authenticated user doesn't have a driver profile
     */
    @Override
    public Driver getCurrentDriver() {
        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return driverRepository.findByUser(user)
                .orElseThrow(()->
                        new RuntimeException("Driver not found with id "+ user.getId()));
    }

    /**
     * Updates the availability status of a driver.
     * 
     * This method manages driver availability for ride assignment.
     * Available drivers can accept new ride requests, while unavailable
     * drivers are excluded from driver matching algorithms.
     * 
     * Common usage scenarios:
     * - Set to false when driver accepts a ride
     * - Set to true when ride is completed or cancelled
     * - Manual availability management by driver
     * 
     * @param driver The driver whose availability to update
     * @param isAvailable The new availability status
     * @return Driver The updated driver entity
     */
    @Override
    public Driver updateDriverAvailability(Driver driver, boolean isAvailable) {
        driver.setIsAvailable(isAvailable);

        return driverRepository.save(driver);
    }

    /**
     * Creates a new driver profile in the system.
     * 
     * This method persists a new driver entity to the database,
     * typically called during the driver onboarding process.
     * The driver should be properly initialized with required
     * fields before calling this method.
     * 
     * @param driver The driver entity to create
     * @return Driver The created driver entity with generated ID
     */
    @Override
    public Driver createNewDriver(Driver driver) {
        return driverRepository.save(driver);
    }
}
