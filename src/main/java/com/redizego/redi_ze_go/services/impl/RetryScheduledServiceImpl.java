package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.PointDto;
import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.entities.RetryRequest;
import com.redizego.redi_ze_go.entities.enums.RetryStatus;
import com.redizego.redi_ze_go.repositories.RetryRequestRepository;
import com.redizego.redi_ze_go.services.RetryScheduledService;
import com.redizego.redi_ze_go.services.RiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Scheduled service implementation for handling retry logic for failed ride requests.
 * 
 * This service runs periodically to process pending retry requests that failed
 * during initial processing. It implements a retry mechanism with a maximum
 * retry count to prevent infinite retry loops.
 * 
 * Key Features:
 * - Automatic retry of failed ride requests
 * - Maximum retry limit (3 attempts)
 * - Status tracking (PENDING -> SUCCESS/FAILED)
 * - Scheduled execution every 5 seconds
 * - Coordinate transformation from PostGIS Point to PointDto
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryScheduledServiceImpl implements RetryScheduledService {

    private final RetryRequestRepository retryRequestRepository;
    private final RiderService riderService;

    /**
     * Processes all pending retry requests in the database.
     * 
     * This method runs on a fixed delay schedule (every 5 seconds) to:
     * 1. Fetch all retry requests with PENDING status
     * 2. Attempt to process each request as a new ride request
     * 3. Update status to SUCCESS if processing succeeds
     * 4. Increment retry count and mark as FAILED after 3 attempts
     * 
     * The method handles coordinate transformation from PostGIS Point geometry
     * to PointDto format required by the ride request processing system.
     * 
     * @implNote Uses Spring's @Scheduled annotation with fixedDelay = 5000ms
     * @implNote Processes requests sequentially to avoid database concurrency issues
     */
    @Scheduled(fixedDelay = 5000) // Retry every 5 seconds
    @Override
    public void retryPendingRequests() {
        // Fetch all retry requests that are still pending
        List<RetryRequest> retryRequestList = retryRequestRepository.findByRetryStatus(RetryStatus.PENDING);
        
        if (retryRequestList.isEmpty()) {
            log.debug("No pending retry requests found");
            return;
        }
        
        log.info("Processing {} pending retry requests", retryRequestList.size());
        
        // Process each retry request
        for (RetryRequest retryRequest : retryRequestList) {
            processRetryRequest(retryRequest);
        }
        
        log.info("Completed processing all pending retry requests");
    }
    
    /**
     * Processes a single retry request.
     * 
     * This method attempts to convert a RetryRequest entity back into
     * a RideRequestDto and process it through the normal ride request flow.
     * 
     * @param retryRequest The retry request to process
     */
    private void processRetryRequest(RetryRequest retryRequest) {
        try {
            log.debug("Processing retry request ID: {} (attempt {})", 
                    retryRequest.getId(), retryRequest.getRetryCount() + 1);
            
            // Convert PostGIS Point to coordinate array for pickup location
            Point pickup = retryRequest.getPickupLocation();
            double[] pickupCoords = new double[] { pickup.getY(), pickup.getX() };
            
            // Convert PostGIS Point to coordinate array for destination location
            Point dropoff = retryRequest.getDestinationLocation();
            double[] dropoffCoords = new double[] { dropoff.getY(), dropoff.getX() };
            
            // Create RideRequestDto with converted coordinates
            RideRequestDto rideRequestDto = new RideRequestDto();
            rideRequestDto.setPickupLocation(new PointDto(pickupCoords));
            rideRequestDto.setDestinationLocation(new PointDto(dropoffCoords));
            
            // Note: Rider information might need to be set based on business logic
            // TODO: Set rider information from retryRequest.getRider() if needed
            
            log.debug("Attempting to process retry request with pickup: [{}, {}], dropoff: [{}, {}]",
                    pickupCoords[0], pickupCoords[1], dropoffCoords[0], dropoffCoords[1]);
            
            // Process the ride request through the normal flow
            riderService.handleOnlineRideRequest(rideRequestDto);
            
            // Mark as successful if no exception was thrown
            retryRequest.setRetryStatus(RetryStatus.SUCCESS);
            log.info("Successfully processed retry request ID: {}", retryRequest.getId());
            
        } catch (Exception e) {
            // Handle failure: increment retry count and check if max attempts reached
            int newRetryCount = retryRequest.getRetryCount() + 1;
            retryRequest.setRetryCount(newRetryCount);
            
            if (newRetryCount >= MAX_RETRY_ATTEMPTS) {
                // Mark as permanently failed after max attempts
                retryRequest.setRetryStatus(RetryStatus.FAILED);
                log.error("Retry request ID: {} failed after {} attempts. Marking as FAILED. Error: {}", 
                        retryRequest.getId(), newRetryCount, e.getMessage());
            } else {
                log.warn("Retry request ID: {} failed on attempt {} of {}. Will retry. Error: {}", 
                        retryRequest.getId(), newRetryCount, MAX_RETRY_ATTEMPTS, e.getMessage());
            }
        } finally {
            // Always save the updated retry request
            retryRequestRepository.save(retryRequest);
        }
    }
    
    /** Maximum number of retry attempts before marking a request as permanently failed */
    private static final int MAX_RETRY_ATTEMPTS = 3;
}
