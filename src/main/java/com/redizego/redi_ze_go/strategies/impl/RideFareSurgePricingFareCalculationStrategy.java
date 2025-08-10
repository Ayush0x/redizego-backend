package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.services.DistanceService;
import com.redizego.redi_ze_go.strategies.RideFareCalculationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("rideFareSurgePricingFareCalculationStrategy")
@RequiredArgsConstructor
public class RideFareSurgePricingFareCalculationStrategy implements RideFareCalculationStrategy {

    private final DistanceService distanceService;
    private static final double SURGE_PENALTY_MULTIPLIER = 1.2;
    @Override
    public double calculateFare(RideRequest rideRequest) {
        double distance=distanceService.calculateDistance(rideRequest.getPickupLocation(),rideRequest.getDestinationLocation());

        return  distance * RIDE_FARE_MULTIPLIER*SURGE_PENALTY_MULTIPLIER;
    }
}
