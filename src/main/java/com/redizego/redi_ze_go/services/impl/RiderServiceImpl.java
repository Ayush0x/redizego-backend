package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.entities.*;
import com.redizego.redi_ze_go.entities.enums.PaymentMethods;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.repositories.RideRequestRepository;
import com.redizego.redi_ze_go.repositories.RiderRepository;
import com.redizego.redi_ze_go.services.DriverService;
import com.redizego.redi_ze_go.services.RatingService;
import com.redizego.redi_ze_go.services.RideService;
import com.redizego.redi_ze_go.services.RiderService;
import com.redizego.redi_ze_go.strategies.RideStrategyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.List;

/**
 * Service implementation for rider-related business operations.
 * 
 * This service manages all rider functionality including ride requests, cancellations,
 * driver rating, profile management, and ride history. The service implements adaptive
 * behavior based on network conditions, switching between online and offline processing
 * modes to ensure reliability even with poor connectivity.
 * 
 * Key Features:
 * - Network-aware ride request processing (online/offline modes)
 * - Ride cancellation with business rule validation
 * - Driver rating system with ride completion verification
 * - Paginated ride history retrieval
 * - Automatic fare calculation using strategy patterns
 * - Driver matching based on rider ratings
 * - Retry mechanism for offline requests with exponential backoff
 * 
 * The service uses network speed thresholds to determine processing mode:
 * - Download threshold: 259 Kbps
 * - Upload threshold: 519 Kbps
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiderServiceImpl implements RiderService {

    /** ModelMapper for entity-DTO conversions */
    private final ModelMapper modelMapper;
    
    /** Strategy manager for ride-related business logic (fare calculation, driver matching, etc.) */
    private final RideStrategyManager rideStrategyManager;
    
    /** Repository for ride request data access */
    private final RideRequestRepository rideRequestRepository;
    
    /** Repository for rider data access */
    private final RiderRepository riderRepository;
    
    /** Service for ride management operations */
    private final RideService rideService;
    
    /** Service for driver-related operations */
    private final DriverService driverService;
    
    /** Service for rating management */
    private final RatingService ratingService;

    /** Network download speed threshold in Kbps for determining offline/online mode */
    private final Double DOWNLOAD_SPEED_THRESHOLD = 259.0; // in Kbps
    
    /** Network upload speed threshold in Kbps for determining offline/online mode */
    private final Double UPLOAD_SPEED_THRESHOLD = 519.0; // in Kbps

    /**
     * Processes a ride request with adaptive network-aware handling.
     * 
     * This method automatically detects network conditions and routes the request
     * to either online or offline processing mode. The decision is based on
     * configurable network speed thresholds to ensure optimal user experience
     * regardless of connectivity quality.
     * 
     * Online mode: Full driver matching, real-time updates, multiple payment options
     * Offline mode: Simplified processing, cash-only payments, retry mechanism
     * 
     * @param rideRequestDto The ride request details containing pickup/destination locations
     * @return RideRequestDto The processed ride request with assigned ID, fare, and status
     * @throws RuntimeException if location validation fails or processing errors occur
     */
    @Override
    @Transactional
    public RideRequestDto requestRide(RideRequestDto rideRequestDto) {
        if(rideStrategyManager.networkStrategy().isNetworkWeak(DOWNLOAD_SPEED_THRESHOLD, UPLOAD_SPEED_THRESHOLD)) {
            log.error("Network is weak, swwitching to offline mode");
            return handleOfflineRideRequest(rideRequestDto);
        }
        else {
            log.info("Network is strong, switching to online mode");
            return handleOnlineRideRequest(rideRequestDto);
        }
    }

    /**
     * Cancels a confirmed ride with comprehensive validation.
     * 
     * This method allows riders to cancel their rides before they start,
     * ensuring proper business rule enforcement and resource cleanup.
     * The cancellation process includes:
     * 1. Ownership verification (only the requesting rider can cancel)
     * 2. Status validation (only CONFIRMED rides can be cancelled)
     * 3. Driver availability restoration
     * 4. Status update to CANCELLED
     * 
     * @param rideId The unique identifier of the ride to cancel
     * @return RideDto The cancelled ride details
     * @throws RuntimeException if rider doesn't own the ride or ride status is invalid
     */
    @Override
    public RideDto cancelRide(Long rideId) {
        Rider rider=getCurrentRider();
        Ride ride=rideService.getRideById(rideId);

        if(!rider.equals(ride.getRider())){
            throw new RuntimeException("Rider cannot cancel this ride as he has not requested the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.CONFIRMED)){
            throw new RuntimeException("Ride cannot be cancelled as the ride status is not invalid "+ride.getRideStatus());
        }

        Ride savedRide=rideService.updateRideStatus(ride,RideStatus.CANCELLED);

        driverService.updateDriverAvailability(ride.getDriver(),true);

        return modelMapper.map(savedRide,RideDto.class);
    }

    /**
     * Submits a rating for the driver after ride completion.
     * 
     * This method allows riders to rate their driver's performance
     * after a successful ride completion. The rating system ensures:
     * 1. Only the rider who took the ride can submit ratings
     * 2. Ratings can only be submitted for ENDED rides
     * 3. Ratings contribute to driver's overall rating calculation
     * 
     * The rating is typically on a scale (e.g., 1-5 stars) and affects
     * the driver's profile and future ride matching algorithms.
     * 
     * @param driverId The unique identifier of the ride (note: parameter name suggests driver ID but implementation uses ride ID)
     * @param rating The rating value to assign to the driver
     * @return DriverDto The updated driver information including new rating
     * @throws RuntimeException if rider doesn't own the ride or ride is not completed
     */
    @Override
    public DriverDto rateDriver(Long driverId, Integer rating) {
        Ride ride=rideService.getRideById(driverId);
        Rider rider=getCurrentRider();

        if(!rider.equals(ride.getRider())) {
            throw new RuntimeException("Rider cannot rate this driver as he has not requested the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.ENDED)){
            throw new RuntimeException("Rider cannot rate this driver as the ride status is not ENDED "+ride.getRideStatus());
        }

        return ratingService.rateDriver(ride,rating);
    }

    /**
     * Retrieves the current authenticated rider's profile information.
     * 
     * This method returns the complete rider profile including personal details,
     * rating, and other relevant information based on the currently authenticated
     * user context.
     * 
     * @return RiderDto The current rider's profile data
     * @throws ResourceNotFoundException if the authenticated user is not found as a rider
     */
    @Override
    public RiderDto getMyProfile() {
        Rider rider=getCurrentRider();
        return modelMapper.map(rider,RiderDto.class);
    }

    /**
     * Retrieves a paginated list of all rides for the current rider.
     * 
     * This method provides access to the rider's complete ride history
     * with pagination support for efficient data loading and display.
     * The results include all ride statuses (completed, cancelled, etc.)
     * and are typically sorted by most recent first.
     * 
     * @param pageRequest Pagination parameters (page number, size, sorting)
     * @return Page<RideDto> Paginated collection of the rider's rides
     * @throws ResourceNotFoundException if the authenticated user is not found as a rider
     */
    @Override
    public Page<RideDto> getAllRides(PageRequest pageRequest) {
        Rider rider=getCurrentRider();


        return rideService.getAllRidesOfRider(rider,pageRequest).map(
                ride->modelMapper.map(ride,RideDto.class)
        );
    }

    /**
     * Creates a new rider profile for a registered user.
     * 
     * This method initializes a rider profile with default settings
     * when a user signs up or gets onboarded as a rider. The new rider
     * starts with a neutral rating of 0.0.
     * 
     * @param user The user entity for whom to create a rider profile
     * @return Rider The created rider entity with generated ID
     */
    @Override
    public Rider createNewRider(User user) {
        Rider rider=Rider
                .builder()
                .user(user)
                .rating(0.0)
                .build();
        return riderRepository.save(rider);
    }

    /**
     * Retrieves the currently authenticated rider from the security context.
     * 
     * This method extracts the authenticated user from Spring Security's
     * SecurityContext and fetches the corresponding rider profile.
     * Used throughout the service to ensure operations are performed
     * by the correct rider.
     * 
     * @return Rider The authenticated rider entity
     * @throws ResourceNotFoundException if the authenticated user doesn't have a rider profile
     */
    public Rider getCurrentRider(){
        User  user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return riderRepository.findByUser(user)
                .orElseThrow(()->
                        new ResourceNotFoundException("Rider not found with id "+ user.getId()));
    }

    /**
     * Handles ride requests in online mode with full feature support.
     * 
     * This method processes ride requests when network conditions are favorable,
     * enabling complete functionality including:
     * 1. Dynamic fare calculation using configured strategy
     * 2. Intelligent driver matching based on rider rating
     * 3. Real-time processing and updates
     * 4. Full payment method support
     * 
     * The method uses strategy patterns for both fare calculation and driver
     * matching to ensure flexible and configurable business logic.
     * 
     * @param rideRequestDto The ride request details to process
     * @return RideRequestDto The processed ride request with fare and matching drivers
     */
    @Override
    public RideRequestDto handleOnlineRideRequest(RideRequestDto rideRequestDto) {
        Rider rider=getCurrentRider();
        RideRequest rideRequest=modelMapper.map(rideRequestDto,RideRequest.class);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        rideRequest.setRider(rider);

        Double fare=rideStrategyManager.rideFareCalculationStrategy().calculateFare(rideRequest);
        rideRequest.setFare(fare);

        RideRequest savedRideRequest=rideRequestRepository.save(rideRequest);

        List<Driver> drivers=rideStrategyManager
                .driverMatchingStrategy(rider.getRating()).findMatchingDrivers(rideRequest);

        return modelMapper.map(savedRideRequest,RideRequestDto.class);
    }

    /**
     * Handles ride requests in offline mode with simplified processing and retry logic.
     * 
     * This method processes ride requests when network conditions are poor,
     * implementing a simplified workflow with automatic retry capabilities:
     * 
     * Features:
     * - Strict location validation (non-null, different pickup/drop points)
     * - Automatic cash payment method assignment for reliability
     * - Fare calculation using the same strategy as online mode
     * - Exponential backoff retry mechanism (3 attempts max)
     * - Comprehensive logging for debugging connectivity issues
     * 
     * Retry Configuration:
     * - Maximum attempts: 3
     * - Initial delay: 2000ms
     * - Backoff multiplier: 1.5x
     * 
     * @param rideRequestDto The ride request details to process
     * @return RideRequestDto The processed ride request with cash payment method
     * @throws RuntimeException if location validation fails or after max retry attempts
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5) // Retry with exponential backoff
    )
    @Override
    public RideRequestDto handleOfflineRideRequest(RideRequestDto rideRequestDto) {
        log.info("Handling offline ride request");

        PointDto pickupLocation = rideRequestDto.getPickupLocation();
        PointDto dropLocation = rideRequestDto.getDestinationLocation();
        if(pickupLocation==null || dropLocation==null){
            throw new RuntimeException("Pickup and drop locations cannot be null for offline ride request");
        }

        if(pickupLocation==dropLocation){
            throw new RuntimeException("Pickup and drop locations cannot be same for offline ride request");
        }

        log.info("Pickup Location: {}, Drop Location: {}", pickupLocation, dropLocation);

        Rider rider = getCurrentRider();
        RideRequest rideRequest = modelMapper.map(rideRequestDto, RideRequest.class);

        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        rideRequest.setRider(rider);

        Double fare = rideStrategyManager.rideFareCalculationStrategy().calculateFare(rideRequest);
        rideRequest.setFare(fare);
        rideRequest.setPaymentMethod(PaymentMethods.CASH);

        RideRequest savedRideRequest = rideRequestRepository.save(rideRequest);

        return modelMapper.map(savedRideRequest, RideRequestDto.class);
    }
}
