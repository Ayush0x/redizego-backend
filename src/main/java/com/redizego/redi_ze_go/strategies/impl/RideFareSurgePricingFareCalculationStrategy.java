package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.strategies.RideFareCalculationStrategy;
import org.springframework.stereotype.Service;

//@Service("rideFareSurgePricingFareCalculationStrategy")
public class RideFareSurgePricingFareCalculationStrategy implements RideFareCalculationStrategy {
    @Override
    public double calculateFare(RideRequest rideRequest) {
        return 0;
    }
}
