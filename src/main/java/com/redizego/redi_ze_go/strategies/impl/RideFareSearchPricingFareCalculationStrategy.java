package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.strategies.RideFareCalculationStrategy;
import org.springframework.stereotype.Service;

@Service
public class RideFareSearchPricingFareCalculationStrategy implements RideFareCalculationStrategy {
    @Override
    public double calculateFare(RideRequestDto rideRequestDto) {
        return 0;
    }
}
