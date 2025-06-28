package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.strategies.DriverMatchigStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverMatchingNearestDriverStrategy implements DriverMatchigStrategy {

    @Override
    public List<Driver> findMatchingDrivers(RideRequestDto rideRequestDto) {
        return List.of();
    }
}
