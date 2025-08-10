package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.strategies.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Strategy manager for ride-related business logic decisions.
 * 
 * This class serves as a central coordinator for selecting appropriate strategies
 * based on various business conditions and rules. It implements the Strategy Pattern
 * by choosing between different implementations at runtime based on context.
 * 
 * Key Responsibilities:
 * - Driver matching strategy selection based on rider rating
 * - Fare calculation strategy selection based on time-based surge pricing
 * - Network strategy coordination for ride processing
 * 
 * Strategy Selection Logic:
 * 
 * Driver Matching:
 * - High-rated riders (≥4.8): Get highest-rated drivers first
 * - Regular riders (<4.8): Get nearest available drivers
 * 
 * Fare Calculation:
 * - Surge hours (6 PM - 10 PM): Apply surge pricing
 * - Regular hours: Apply standard fare calculation
 * 
 * Business Rules:
 * - Premium service for loyal, high-rated customers
 * - Dynamic pricing based on demand patterns
 * - Time-based surge pricing implementation
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see DriverMatchingStrategy
 * @see RideFareCalculationStrategy
 * @see NetworkStrategy
 * 
 * @implNote Uses dependency injection to access all available strategy implementations
 * @implNote Strategy selection is performed at runtime based on current conditions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RideStrategyManager {

    // Driver matching strategies
    private final DriverMatchingHighestRatedDriverStrategy driverMatchingHighestRatedDriverStrategy;
    private final DriverMatchingNearestDriverStrategy driverMatchingNearestDriverStrategy;
    
    // Fare calculation strategies
    private final RideFareDefaultFareCalculationStrategy rideFareDefaultFareCalculationStrategy;
    private final RideFareSurgePricingFareCalculationStrategy rideFareSurgePricingFareCalculationStrategy;
    
    // Network strategy
    private final NetworkStrategyImpl networkStrategy;
    
    // Business rule constants
    /** Minimum rider rating to qualify for premium driver matching */
    private static final double PREMIUM_RIDER_RATING_THRESHOLD = 4.8;
    
    /** Start time for surge pricing (6:00 PM) */
    private static final LocalTime SURGE_START_TIME = LocalTime.of(18, 0);
    
    /** End time for surge pricing (10:00 PM) */
    private static final LocalTime SURGE_END_TIME = LocalTime.of(22, 0);

    /**
     * Selects appropriate driver matching strategy based on rider's rating.
     * 
     * This method implements a tiered service approach where high-rated riders
     * receive premium service by getting matched with highly-rated drivers first,
     * while regular riders get matched based on proximity for efficiency.
     * 
     * Strategy Selection Logic:
     * - Rating ≥ 4.8: DriverMatchingHighestRatedDriverStrategy
     *   → Prioritizes driver quality over distance
     *   → Rewards loyal customers with better service
     * - Rating < 4.8: DriverMatchingNearestDriverStrategy
     *   → Prioritizes quick pickup times
     *   → Standard service level
     * 
     * @param riderRating The current rating of the rider requesting the ride
     * @return DriverMatchingStrategy implementation based on rider rating
     * 
     * @implNote Rating threshold can be adjusted based on business requirements
     * @implNote Premium service encourages good rider behavior and retention
     */
    public DriverMatchingStrategy driverMatchingStrategy(double riderRating) {
        if (riderRating >= PREMIUM_RIDER_RATING_THRESHOLD) {
            log.debug("Selected highest-rated driver strategy for premium rider with rating: {}", riderRating);
            return driverMatchingHighestRatedDriverStrategy;
        } else {
            log.debug("Selected nearest driver strategy for rider with rating: {}", riderRating);
            return driverMatchingNearestDriverStrategy;
        }
    }

    /**
     * Selects appropriate fare calculation strategy based on current time.
     * 
     * This method implements time-based surge pricing to manage demand during
     * peak hours. Higher fares during busy periods help balance supply and demand
     * while ensuring driver availability.
     * 
     * Surge Pricing Schedule:
     * - Surge Hours: 6:00 PM - 10:00 PM daily
     *   → RideFareSurgePricingFareCalculationStrategy
     *   → Higher fares to incentivize driver availability
     * - Regular Hours: All other times
     *   → RideFareDefaultFareCalculationStrategy
     *   → Standard distance-based pricing
     * 
     * @return RideFareCalculationStrategy implementation based on current time
     * 
     * @implNote Surge hours can be adjusted based on local demand patterns
     * @implNote Future enhancements could include day-of-week and location-based rules
     */
    public RideFareCalculationStrategy rideFareCalculationStrategy() {
        LocalTime currentTime = LocalTime.now();
        
        // Check if current time falls within surge pricing hours
        boolean isSurgeTime = currentTime.isAfter(SURGE_START_TIME) && 
                              currentTime.isBefore(SURGE_END_TIME);
        
        if (isSurgeTime) {
            log.debug("Selected surge pricing strategy for current time: {} (surge period: {} - {})", 
                    currentTime, SURGE_START_TIME, SURGE_END_TIME);
            return rideFareSurgePricingFareCalculationStrategy;
        } else {
            log.debug("Selected default fare calculation strategy for current time: {}", currentTime);
            return rideFareDefaultFareCalculationStrategy;
        }
    }

    /**
     * Returns the network strategy implementation for ride processing.
     * 
     * This method provides access to the network strategy which handles
     * communication and coordination between different components of the
     * ride processing system.
     * 
     * The network strategy is responsible for:
     * - Coordinating between services
     * - Managing external API calls
     * - Handling network communication protocols
     * 
     * @return NetworkStrategyImpl instance for network operations
     * 
     * @implNote Currently returns a single implementation
     * @implNote Future versions could support multiple network strategies
     */
    public NetworkStrategyImpl networkStrategy() {
        log.debug("Returning network strategy implementation");
        return networkStrategy;
    }
}
