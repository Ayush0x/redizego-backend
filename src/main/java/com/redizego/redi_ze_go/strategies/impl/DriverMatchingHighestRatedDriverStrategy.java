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
 * Premium driver matching strategy that prioritizes highly-rated drivers.
 * 
 * This strategy implementation provides premium service to high-rated riders by
 * matching them with the highest-rated available drivers in the vicinity. It balances
 * service quality with reasonable proximity to ensure good pickup times.
 * 
 * Matching Algorithm:
 * 1. Find drivers within reasonable distance of pickup location
 * 2. Filter by availability status
 * 3. Sort by driver rating (highest first)
 * 4. Return top 10 highest-rated drivers
 * 
 * Business Logic:
 * - Prioritizes service quality over absolute proximity
 * - Rewards high-performing drivers with premium ride opportunities
 * - Provides better experience for loyal, high-rated riders
 * - Maintains reasonable pickup times by limiting search radius
 * 
 * Use Cases:
 * - High-rated riders (rating ≥ 4.8) as determined by RideStrategyManager
 * - Premium service tier offerings
 * - Quality-focused ride matching scenarios
 * 
 * Performance Considerations:
 * - Uses spatial indexing for efficient nearby driver queries
 * - Limits result set to 10 drivers to optimize response time
 * - Combines proximity and rating filters in single database query
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see DriverMatchingStrategy
 * @see com.redizego.redi_ze_go.strategies.impl.DriverMatchingNearestDriverStrategy
 * @see com.redizego.redi_ze_go.strategies.RideStrategyManager
 * @see com.redizego.redi_ze_go.repositories.DriverRepository#findTenNearbyTopRatedDrivers
 * 
 * @implNote Registered as Spring bean with name "driverMatchingHighestRatedDriverStrategy"
 * @implNote Uses PostGIS spatial queries for efficient geographic operations
 */
@Slf4j
@Service("driverMatchingHighestRatedDriverStrategy")
@RequiredArgsConstructor
public class DriverMatchingHighestRatedDriverStrategy implements DriverMatchingStrategy {

    /** Repository for accessing driver data with spatial queries */
    private final DriverRepository driverRepository;
    
    /**
     * Finds the highest-rated available drivers near the pickup location.
     * 
     * This method implements a quality-first matching algorithm that finds nearby
     * drivers and sorts them by rating to provide the best possible service experience.
     * The spatial query considers both proximity and driver rating to optimize for
     * service quality while maintaining reasonable pickup times.
     * 
     * Algorithm Details:
     * 1. Execute spatial query to find drivers within service radius
     * 2. Filter by availability status (isAvailable = true)
     * 3. Sort results by driver rating in descending order
     * 4. Limit results to top 10 drivers for optimal selection
     * 
     * @param rideRequest The ride request containing pickup location and details
     * @return List of up to 10 highest-rated available drivers sorted by rating
     * @throws IllegalArgumentException if rideRequest is null or pickup location is invalid
     * @throws RuntimeException if spatial query execution fails
     * 
     * @implNote Uses PostGIS ST_DWithin function for efficient spatial filtering
     * @implNote Query combines proximity and rating sorting for optimal performance
     * 
     * @example
     * RideRequest request = new RideRequest(pickupPoint, destination);
     * List<Driver> topDrivers = strategy.findMatchingDrivers(request);
     * // Returns drivers sorted by rating: [4.9, 4.8, 4.7, ...]
     */
    @Override
    public List<Driver> findMatchingDrivers(RideRequest rideRequest) {
        if (rideRequest == null) {
            log.error("Cannot find matching drivers: ride request is null");
            throw new IllegalArgumentException("Ride request cannot be null");
        }
        
        if (rideRequest.getPickupLocation() == null) {
            log.error("Cannot find matching drivers: pickup location is null");
            throw new IllegalArgumentException("Pickup location cannot be null");
        }
        
        try {
            log.debug("Finding highest-rated drivers near pickup location: {}", 
                    rideRequest.getPickupLocation());
            
            // Query for nearby top-rated available drivers
            List<Driver> topRatedDrivers = driverRepository.findTenNearbyTopRatedDrivers(
                    rideRequest.getPickupLocation());
            
            log.info("Found {} highly-rated drivers for ride request ID: {}", 
                    topRatedDrivers.size(), rideRequest.getId());
            
            if (topRatedDrivers.isEmpty()) {
                log.warn("No highly-rated drivers available near pickup location: {}", 
                        rideRequest.getPickupLocation());
            } else {
                // Log rating range for debugging
                double highestRating = topRatedDrivers.stream()
                        .mapToDouble(Driver::getRating)
                        .max()
                        .orElse(0.0);
                double lowestRating = topRatedDrivers.stream()
                        .mapToDouble(Driver::getRating)
                        .min()
                        .orElse(0.0);
                
                log.debug("Driver rating range: {:.1f} - {:.1f} for {} drivers", 
                        lowestRating, highestRating, topRatedDrivers.size());
            }
            
            return topRatedDrivers;
            
        } catch (Exception e) {
            log.error("Error finding highest-rated drivers for ride request ID: {}", 
                    rideRequest.getId(), e);
            throw new RuntimeException("Failed to find matching drivers", e);
        }
    }
}
