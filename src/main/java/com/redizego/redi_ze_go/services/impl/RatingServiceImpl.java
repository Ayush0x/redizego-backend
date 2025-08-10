package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Rating;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.repositories.DriverRepository;
import com.redizego.redi_ze_go.repositories.RatingRepository;
import com.redizego.redi_ze_go.repositories.RiderRepository;
import com.redizego.redi_ze_go.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Service implementation for rating and feedback management.
 * 
 * This service handles the bidirectional rating system between drivers and riders,
 * including rating submission, validation, average calculation, and profile updates.
 * It ensures fair and accurate rating management while preventing duplicate ratings
 * and maintaining data integrity.
 * 
 * Key Features:
 * - Bidirectional rating (driver ↔ rider)
 * - Duplicate rating prevention
 * - Real-time rating average calculation
 * - Profile rating updates
 * - Rating record initialization
 * - Comprehensive validation and error handling
 * 
 * Rating System Rules:
 * - Only completed rides can be rated
 * - Each party can rate only once per ride
 * - Ratings affect overall profile scores
 * - Ratings are calculated using simple averages
 * 
 * The rating system enhances trust and quality by:
 * - Encouraging good behavior from both parties
 * - Providing feedback for service improvement
 * - Helping users make informed decisions
 * - Supporting driver matching algorithms
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    /** Repository for rating data access and persistence */
    private final RatingRepository ratingRepository;
    
    /** Repository for driver profile updates */
    private final DriverRepository driverRepository;
    
    /** Repository for rider profile updates */
    private final RiderRepository riderRepository;
    
    /** ModelMapper for entity-DTO conversions */
    private final ModelMapper modelMapper;

    /**
     * Submits a rating for a driver and updates their overall rating.
     * 
     * This method handles driver rating submission with comprehensive validation:
     * 1. Retrieves existing rating record for the ride
     * 2. Validates no duplicate rating exists
     * 3. Records the driver rating
     * 4. Recalculates driver's overall rating average
     * 5. Updates driver profile with new rating
     * 
     * The rating calculation uses all historical ratings for the driver
     * to compute a fair average that reflects their service quality.
     * 
     * @param ride The completed ride for which to rate the driver
     * @param rating The rating score to assign (typically 1-5 scale)
     * @return DriverDto Updated driver profile with new rating
     * @throws RuntimeException if rating record doesn't exist or driver already rated
     */
    @Override
    public DriverDto rateDriver(Ride ride, Integer rating) {
        Driver driver=ride.getDriver();
        Rating ratingObject=ratingRepository.findByRide(ride)
                .orElseThrow(
                        () -> new RuntimeException("Cannot rate as the ride does not exist"));

        if(ratingObject.getDriverRating()!=null){
            throw new RuntimeException("Driver has already been rated by this rider");
        }

        ratingObject.setDriverRating(rating);
        ratingRepository.save(ratingObject);

        Double newRating=ratingRepository.findByDriver(driver)
                .stream().mapToDouble(Rating::getDriverRating)
                .average()
                .orElse(0.0);

        driver.setRating(newRating);
        Driver savedDriver=driverRepository.save(driver);

        return modelMapper.map(savedDriver, DriverDto.class);
    }

    /**
     * Submits a rating for a rider and updates their overall rating.
     * 
     * This method handles rider rating submission with comprehensive validation:
     * 1. Retrieves existing rating record for the ride
     * 2. Validates no duplicate rating exists
     * 3. Records the rider rating
     * 4. Recalculates rider's overall rating average
     * 5. Updates rider profile with new rating
     * 
     * The rating calculation uses all historical ratings for the rider
     * to compute a fair average that reflects their behavior quality.
     * 
     * @param ride The completed ride for which to rate the rider
     * @param rating The rating score to assign (typically 1-5 scale)
     * @return RiderDto Updated rider profile with new rating
     * @throws RuntimeException if rating record doesn't exist or rider already rated
     */
    @Override
    public RiderDto rateRider(Ride ride, Integer rating) {
        Rider rider=ride.getRider();
        Rating ratingObject=ratingRepository.findByRide(ride)
                .orElseThrow(
                        () -> new RuntimeException("Cannot rate as the ride does not exist"));

        if(ratingObject.getRiderRating()!=null){
            throw new RuntimeException("Rider has already been rated by this driver");
        }

        ratingObject.setRiderRating(rating);
        ratingRepository.save(ratingObject);

        Double newRating=ratingRepository.findByRider(rider)
                .stream().mapToDouble(Rating::getRiderRating)
                .average()
                .orElse(0.0);

        rider.setRating(newRating);
        Rider savedRider=riderRepository.save(rider);

        return modelMapper.map(savedRider, RiderDto.class);
    }

    /**
     * Creates a new rating record for a ride when it starts.
     * 
     * This method initializes an empty rating record that will be populated
     * later when either the driver or rider submits their rating after
     * ride completion. The record serves as a placeholder and prevents
     * duplicate rating attempts.
     * 
     * The rating record includes:
     * - Associated ride information
     * - Rider and driver references
     * - Null rating values (to be filled later)
     * 
     * @param ride The ride for which to create a rating record
     */
    @Override
    public void createNewRating(Ride ride) {
        Rating rating=Rating.builder()
                .rider(ride.getRider())
                .driver(ride.getDriver())
                .ride(ride)
                .build();

        ratingRepository.save(rating);
    }
}
