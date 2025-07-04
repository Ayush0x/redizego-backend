package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.entities.RideRequest;

public interface RideFareCalculationStrategy {

    double RIDE_FARE_MULTIPLIER=10;
    double calculateFare(RideRequest rideRequest);
}
