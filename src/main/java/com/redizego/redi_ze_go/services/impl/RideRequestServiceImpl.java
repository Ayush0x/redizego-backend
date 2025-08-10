package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.repositories.RideRequestRepository;
import com.redizego.redi_ze_go.services.RideRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service implementation for ride request management operations.
 * 
 * This service handles basic CRUD operations for ride requests,
 * serving as the data access layer for ride request entities.
 * It provides essential functionality for retrieving and updating
 * ride request records during the ride lifecycle.
 * 
 * Key Features:
 * - Ride request retrieval by ID with error handling
 * - Ride request status updates and persistence
 * - Integration with repository layer
 * 
 * Typical Usage Flow:
 * 1. Rider creates ride request (handled by RiderService)
 * 2. Driver retrieves request for acceptance
 * 3. Service updates request status when accepted
 * 4. Request transitions to ride entity
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class RideRequestServiceImpl implements RideRequestService {

    /** Repository for ride request data access and persistence */
    private final RideRequestRepository rideRequestRepository;

    /**
     * Retrieves a ride request by its unique identifier.
     * 
     * This method provides secure access to ride request information
     * with proper error handling for non-existent requests. Used primarily
     * by driver service when accepting ride requests.
     * 
     * @param rideRequestId The unique identifier of the ride request
     * @return RideRequest The ride request entity with all details
     * @throws ResourceNotFoundException if no ride request exists with the given ID
     */
    @Override
    public RideRequest findRideRequestById(Long rideRequestId) {
        return rideRequestRepository.findById(rideRequestId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Ride request not found with id "+ rideRequestId));
    }

    /**
     * Updates an existing ride request in the database.
     * 
     * This method handles ride request updates, typically used when
     * transitioning request status from PENDING to ACCEPTED when
     * a driver accepts the ride request.
     * 
     * The method first validates that the ride request exists
     * before attempting to save the updates.
     * 
     * @param rideRequest The ride request entity with updated information
     * @throws ResourceNotFoundException if the ride request doesn't exist
     */
    @Override
    public void update(RideRequest rideRequest) {
        RideRequest toSave=rideRequestRepository.findById(rideRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride request not found with id "+ rideRequest.getId()));

        rideRequestRepository.save(toSave);
    }
}
