package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.repositories.RideRepository;
import com.redizego.redi_ze_go.services.RideRequestService;
import com.redizego.redi_ze_go.services.RideService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service implementation for ride management and lifecycle operations.
 * 
 * This service handles the core ride entity management including creation,
 * status updates, retrieval, and ride history queries. It serves as the
 * central service for ride-related database operations and business logic.
 * 
 * Key Features:
 * - Ride creation from accepted ride requests
 * - Ride status management throughout lifecycle
 * - Ride retrieval by ID with error handling
 * - Paginated ride history for riders and drivers
 * - Secure OTP generation for ride verification
 * 
 * Ride Status Flow:
 * Request → CONFIRMED → ONGOING → ENDED/CANCELLED
 * 
 * The service integrates with:
 * - RideRequestService for request status updates
 * - Repository layer for data persistence
 * - Security features via OTP generation
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    /** Repository for ride data access and persistence */
    private final RideRepository rideRepository;
    
    /** Service for ride request management and status updates */
    private final RideRequestService rideRequestService;
    
    /** ModelMapper for entity transformations */
    private final ModelMapper modelMapper;

    /**
     * Retrieves a ride by its unique identifier.
     * 
     * This method provides secure access to ride information with
     * proper error handling for non-existent rides.
     * 
     * @param id The unique identifier of the ride to retrieve
     * @return Ride The ride entity with all associated data
     * @throws RuntimeException if no ride exists with the given ID
     */
    @Override
    public Ride getRideById(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(()->
                        new RuntimeException("Ride not found with id "+ id));
    }

    /**
     * Creates a new ride from an accepted ride request.
     * 
     * This method handles the transition from ride request to confirmed ride:
     * 1. Updates the original request status to ACCEPTED
     * 2. Maps request data to ride entity
     * 3. Sets initial ride status to CONFIRMED
     * 4. Assigns the accepting driver
     * 5. Generates secure OTP for ride start verification
     * 6. Persists both request update and new ride
     * 
     * The OTP provides security by ensuring the driver physically
     * meets the rider before starting the trip.
     * 
     * @param rideRequest The accepted ride request containing trip details
     * @param driver The driver who accepted the request
     * @return Ride The newly created ride entity with generated ID and OTP
     */
    @Override
    public Ride createNewRide(RideRequest rideRequest, Driver driver) {
        rideRequest.setRideRequestStatus(RideRequestStatus.ACCEPTED);

        Ride ride=modelMapper.map(rideRequest,Ride.class);
        ride.setRideStatus(RideStatus.CONFIRMED);
        ride.setDriver(driver);
        ride.setOtp(generateOtp());
        ride.setId(null);

        rideRequestService.update(rideRequest);
        return rideRepository.save(ride);
    }

    /**
     * Updates the status of an existing ride.
     * 
     * This method handles ride status transitions throughout the lifecycle:
     * - CONFIRMED → ONGOING (when ride starts)
     * - ONGOING → ENDED (when ride completes)
     * - CONFIRMED/ONGOING → CANCELLED (when ride is cancelled)
     * 
     * Status updates trigger various business processes like payment
     * processing, driver availability changes, and notification sending.
     * 
     * @param ride The ride entity to update
     * @param rideStatus The new status to set
     * @return Ride The updated ride entity
     */
    @Override
    public Ride updateRideStatus(Ride ride, RideStatus rideStatus) {
        ride.setRideStatus(rideStatus);
        return rideRepository.save(ride);
    }

    /**
     * Retrieves a paginated list of all rides for a specific rider.
     * 
     * This method provides ride history access with pagination support
     * for efficient data loading. Results include rides in all statuses
     * and are typically ordered by most recent first.
     * 
     * @param rider The rider whose rides to retrieve
     * @param pageRequest Pagination parameters (page, size, sorting)
     * @return Page<Ride> Paginated collection of rider's rides
     */
    @Override
    public Page<Ride> getAllRidesOfRider(Rider rider, PageRequest pageRequest) {
        return rideRepository.findByRider(rider,pageRequest);
    }

    /**
     * Retrieves a paginated list of all rides for a specific driver.
     * 
     * This method provides ride history access for drivers with pagination
     * support for efficient data loading. Results include rides in all statuses
     * and are typically ordered by most recent first.
     * 
     * @param driver The driver whose rides to retrieve
     * @param pageRequest Pagination parameters (page, size, sorting)
     * @return Page<Ride> Paginated collection of driver's rides
     */
    @Override
    public Page<Ride> getAllRidesOfDriver(Driver driver, PageRequest pageRequest) {
        return rideRepository.findByDriver(driver,pageRequest);
    }

    /**
     * Generates a secure 6-digit OTP for ride verification.
     * 
     * This method creates a random 6-digit numeric code used for
     * ride start verification. The OTP ensures that the driver
     * has physically met the rider before starting the trip,
     * providing security against fraudulent ride starts.
     * 
     * The OTP format is always 6 digits with leading zeros if necessary.
     * 
     * @return String A 6-digit OTP string (e.g., "000123", "456789")
     */
    private String generateOtp() {
        Random random=new Random();
        int otp=random.nextInt(1000000);
        return String.format("%06d", otp);
    }
}
