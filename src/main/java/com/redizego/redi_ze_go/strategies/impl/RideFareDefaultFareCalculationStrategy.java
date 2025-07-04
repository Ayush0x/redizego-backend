package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.services.DistanceService;
import com.redizego.redi_ze_go.strategies.RideFareCalculationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("rideFareDefaultFareCalculationStrategy")
@RequiredArgsConstructor
public class RideFareDefaultFareCalculationStrategy implements RideFareCalculationStrategy {

    private final DistanceService distanceService;


    @Override
    public double calculateFare(RideRequest rideRequest) {
        double distance=distanceService.calculateDistance(rideRequest.getPickupLocation(),rideRequest.getDestinationLocation());

        return distance * RIDE_FARE_MULTIPLIER;
    }
}
