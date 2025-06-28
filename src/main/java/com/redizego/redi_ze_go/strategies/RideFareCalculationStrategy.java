package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.dtos.RideRequestDto;

public interface RideFareCalculationStrategy {

    double calculateFare(RideRequestDto rideRequestDto);
}
