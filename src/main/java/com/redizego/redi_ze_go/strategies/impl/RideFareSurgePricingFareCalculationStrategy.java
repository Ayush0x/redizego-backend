package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.services.DistanceService;
import com.redizego.redi_ze_go.strategies.RideFareCalculationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Surge pricing fare calculation strategy for high-demand periods.
 * 
 * This strategy implements dynamic pricing during peak hours or high-demand situations
 * to balance supply and demand. It applies a surge multiplier to the base fare to
 * incentivize driver availability during busy periods while managing rider demand.
 * 
 * Pricing Formula:
 * Surge Fare = Base Distance Fare × Standard Multiplier × Surge Multiplier
 * Surge Fare = Distance (km) × RIDE_FARE_MULTIPLIER × 1.2
 * 
 * Key Features:
 * - 20% surge premium over standard fares
 * - Distance-based calculation with surge adjustment
 * - Applied during high-demand time windows (6 PM - 10 PM)
 * - Helps balance supply-demand economics
 * 
 * Business Logic:
 * - Increases driver incentives during peak hours
 * - Manages rider demand through price sensitivity
 * - Maintains service availability during busy periods
 * - Provides additional revenue during high-value time slots
 * 
 * Use Cases:
 * - Peak hour pricing (rush hours, evening entertainment)
 * - High-demand events (concerts, sports events)
 * - Weather-related surge (rain, storms)
 * - Special occasions (New Year's Eve, holidays)
 * 
 * Economic Impact:
 * - Balances marketplace dynamics
 * - Ensures driver availability during peak times
 * - Optimizes platform utilization rates
 * - Provides fair compensation for peak-hour service
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see RideFareCalculationStrategy
 * @see com.redizego.redi_ze_go.strategies.impl.RideFareDefaultFareCalculationStrategy
 * @see com.redizego.redi_ze_go.strategies.RideStrategyManager
 * @see com.redizego.redi_ze_go.services.DistanceService
 * 
 * @implNote Registered as Spring bean with name "rideFareSurgePricingFareCalculationStrategy"
 * @implNote Surge multiplier is configurable via static constant
 */
@Slf4j
@Service("rideFareSurgePricingFareCalculationStrategy")
@RequiredArgsConstructor
public class RideFareSurgePricingFareCalculationStrategy implements RideFareCalculationStrategy {

    /** Service for calculating distance between geographic points */
    private final DistanceService distanceService;
    
    /** 
     * Surge pricing multiplier applied during high-demand periods.
     * Current value of 1.2 represents a 20% increase over standard fares.
     */
    private static final double SURGE_PENALTY_MULTIPLIER = 1.2;
    
    /**
     * Calculates surge-adjusted fare for a ride request during high-demand periods.
     * 
     * This method applies surge pricing by calculating the base distance fare and
     * then applying the surge multiplier to account for increased demand. The surge
     * pricing helps balance marketplace economics during peak usage times.
     * 
     * Calculation Steps:
     * 1. Calculate straight-line distance using PostGIS spatial functions
     * 2. Apply base fare rate per kilometer (RIDE_FARE_MULTIPLIER)
     * 3. Apply surge multiplier for demand-based premium
     * 4. Return total surge-adjusted fare
     * 
     * Formula: Fare = Distance × Base Rate × Surge Multiplier
     * 
     * @param rideRequest The ride request containing pickup and destination locations
     * @return The calculated surge fare amount in the system's base currency
     * @throws IllegalArgumentException if rideRequest is null or locations are invalid
     * @throws RuntimeException if distance calculation fails
     * 
     * @implNote Uses great-circle distance for accurate geographic calculations
     * @implNote Surge multiplier provides 20% premium over standard rates
     * 
     * @example
     * For a 10km ride during surge hours:
     * - Base fare: 10km × base_rate = base_amount
     * - Surge fare: base_amount × 1.2 = surge_amount (20% higher)
     */
    @Override
    public double calculateFare(RideRequest rideRequest) {
        if (rideRequest == null) {
            log.error("Cannot calculate surge fare: ride request is null");
            throw new IllegalArgumentException("Ride request cannot be null");
        }
        
        if (rideRequest.getPickupLocation() == null || rideRequest.getDestinationLocation() == null) {
            log.error("Cannot calculate surge fare: pickup or destination location is null");
            throw new IllegalArgumentException("Pickup and destination locations are required");
        }
        
        try {
            // Calculate distance using PostGIS spatial functions
            double distance = distanceService.calculateDistance(
                    rideRequest.getPickupLocation(), 
                    rideRequest.getDestinationLocation()
            );
            
            // Calculate base fare
            double baseFare = distance * RIDE_FARE_MULTIPLIER;
            
            // Apply surge multiplier
            double surgeFare = baseFare * SURGE_PENALTY_MULTIPLIER;
            
            log.debug("Calculated surge fare for ride request ID: {} - Distance: {:.2f}km, " +
                    "Base fare: {:.2f}, Surge multiplier: {:.1f}x, Final fare: {:.2f}", 
                    rideRequest.getId(), distance, baseFare, SURGE_PENALTY_MULTIPLIER, surgeFare);
            
            log.info("Surge pricing applied - Original: {:.2f}, Surge: {:.2f} ({:.0f}% increase)", 
                    baseFare, surgeFare, ((SURGE_PENALTY_MULTIPLIER - 1) * 100));
            
            return surgeFare;
            
        } catch (Exception e) {
            log.error("Failed to calculate surge fare for ride request ID: {}", 
                    rideRequest.getId(), e);
            throw new RuntimeException("Surge fare calculation failed", e);
        }
    }
    
    /**
     * Gets the current surge multiplier value.
     * 
     * @return The surge multiplier (1.2 = 20% increase)
     */
    public static double getSurgeMultiplier() {
        return SURGE_PENALTY_MULTIPLIER;
    }
    
    /**
     * Calculates the percentage increase over standard fare.
     * 
     * @return The surge percentage (e.g., 20.0 for 20% increase)
     */
    public static double getSurgePercentage() {
        return (SURGE_PENALTY_MULTIPLIER - 1.0) * 100.0;
    }
}
