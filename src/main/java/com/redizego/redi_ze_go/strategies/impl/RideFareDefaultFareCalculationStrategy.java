package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.services.DistanceService;
import com.redizego.redi_ze_go.strategies.RideFareCalculationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default fare calculation strategy implementation for ride pricing.
 * 
 * This strategy implements basic fare calculation based on distance traveled
 * between pickup and destination locations. It uses a simple linear pricing
 * model without considering factors like time, demand, or surge pricing.
 * 
 * Calculation Formula:
 * Fare = Distance (in km) × RIDE_FARE_MULTIPLIER
 * 
 * Key Features:
 * - Distance-based pricing using PostGIS spatial calculations
 * - Fixed rate per kilometer for predictable pricing
 * - No surge or time-based pricing components
 * - Suitable for standard ride requests
 * 
 * Use Cases:
 * - Regular ride requests during normal hours
 * - Base fare calculation for other strategies
 * - Testing and development environments
 * - Markets with simple pricing requirements
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see RideFareCalculationStrategy
 * @see com.redizego.redi_ze_go.strategies.impl.RideFareSurgePricingFareCalculationStrategy
 * @see com.redizego.redi_ze_go.services.DistanceService
 * 
 * @implNote Registered as Spring bean with name "rideFareDefaultFareCalculationStrategy"
 */
@Slf4j
@Service("rideFareDefaultFareCalculationStrategy")
@RequiredArgsConstructor
public class RideFareDefaultFareCalculationStrategy implements RideFareCalculationStrategy {

    private final DistanceService distanceService;

    /**
     * Calculates the fare for a ride request using distance-based pricing.
     * 
     * This method computes the straight-line distance between pickup and
     * destination locations using PostGIS spatial functions, then applies
     * the standard fare multiplier to determine the total cost.
     * 
     * @param rideRequest The ride request containing pickup and destination locations
     * @return The calculated fare amount in the system's base currency
     * @throws IllegalArgumentException if rideRequest is null or locations are invalid
     * @throws RuntimeException if distance calculation fails
     * 
     * @implNote Uses great-circle distance calculation for accuracy
     * @implNote Fare is calculated in real-time for each request
     */
    @Override
    public double calculateFare(RideRequest rideRequest) {
        if (rideRequest == null) {
            log.error("Cannot calculate fare: ride request is null");
            throw new IllegalArgumentException("Ride request cannot be null");
        }
        
        if (rideRequest.getPickupLocation() == null || rideRequest.getDestinationLocation() == null) {
            log.error("Cannot calculate fare: pickup or destination location is null");
            throw new IllegalArgumentException("Pickup and destination locations are required");
        }
        
        try {
            // Calculate distance using PostGIS spatial functions
            double distance = distanceService.calculateDistance(
                    rideRequest.getPickupLocation(), 
                    rideRequest.getDestinationLocation()
            );
            
            // Apply standard fare multiplier
            double fare = distance * RIDE_FARE_MULTIPLIER;
            
            log.debug("Calculated fare for ride request ID: {} - Distance: {:.2f}km, Fare: {:.2f}", 
                    rideRequest.getId(), distance, fare);
            
            return fare;
            
        } catch (Exception e) {
            log.error("Failed to calculate fare for ride request ID: {}", 
                    rideRequest.getId(), e);
            throw new RuntimeException("Fare calculation failed", e);
        }
    }
}
