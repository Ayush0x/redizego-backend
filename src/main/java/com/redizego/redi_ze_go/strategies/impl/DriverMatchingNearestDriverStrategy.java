package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.repositories.DriverRepository;
import com.redizego.redi_ze_go.strategies.DriverMatchingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Strategy implementation for finding drivers based on nearest distance to pickup location.
 * 
 * This strategy implements the DriverMatchingStrategy interface and provides
 * driver matching logic based on geographical proximity. It finds the closest
 * available drivers to the ride request pickup location using PostGIS spatial
 * queries for optimal performance.
 * 
 * Key Features:
 * - Uses PostGIS spatial indexing for efficient distance calculations
 * - Returns up to 10 nearest available drivers
 * - Considers driver availability status
 * - Optimized for real-time ride matching
 * 
 * Algorithm:
 * 1. Query database for available drivers using spatial index
 * 2. Calculate distances from pickup location to all driver locations
 * 3. Sort by distance (ascending)
 * 4. Return top 10 closest drivers
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see DriverMatchingStrategy
 * @see com.redizego.redi_ze_go.strategies.impl.DriverMatchingHighestRatedDriverStrategy
 * @see com.redizego.redi_ze_go.repositories.DriverRepository#findTenNearestDrivers
 * 
 * @implNote This strategy is registered as a Spring bean with name "driverMatchingNearestDriverStrategy"
 */
@Slf4j
@Service("driverMatchingNearestDriverStrategy")
@RequiredArgsConstructor
public class DriverMatchingNearestDriverStrategy implements DriverMatchingStrategy {

    private final DriverRepository driverRepository;

    /**
     * Finds the nearest available drivers to the pickup location.
     * 
     * Uses PostGIS spatial functions to efficiently calculate distances
     * between the pickup location and all available driver locations.
     * Returns up to 10 drivers sorted by proximity.
     * 
     * @param rideRequest The ride request containing pickup location
     * @return List of up to 10 nearest available drivers, sorted by distance
     * @throws IllegalArgumentException if rideRequest or pickup location is null
     * 
     * @implNote Uses ST_Distance with SRID 4326 for accurate geographical distance
     * @implNote Only considers drivers with isAvailable = true
     */
    @Override
    public List<Driver> findMatchingDrivers(RideRequest rideRequest) {
        if (rideRequest == null) {
            log.error("RideRequest cannot be null for driver matching");
            throw new IllegalArgumentException("RideRequest cannot be null");
        }
        
        if (rideRequest.getPickupLocation() == null) {
            log.error("Pickup location cannot be null for driver matching");
            throw new IllegalArgumentException("Pickup location cannot be null");
        }
        
        log.debug("Finding nearest drivers for pickup location: {}", 
                rideRequest.getPickupLocation());
        
        try {
            // Query for nearest available drivers using PostGIS spatial functions
            List<Driver> nearestDrivers = driverRepository.findTenNearestDrivers(
                    rideRequest.getPickupLocation());
            
            log.info("Found {} available drivers near pickup location for ride request ID: {}", 
                    nearestDrivers.size(), rideRequest.getId());
            
            if (nearestDrivers.isEmpty()) {
                log.warn("No available drivers found near pickup location: {}", 
                        rideRequest.getPickupLocation());
            }
            
            return nearestDrivers;
            
        } catch (Exception e) {
            log.error("Error finding nearest drivers for ride request ID: {}", 
                    rideRequest.getId(), e);
            throw new RuntimeException("Failed to find matching drivers", e);
        }
    }
}
