package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.strategies.DriverMatchingStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service("driverMatchingHighestRatedDriverStrategy")
public class DriverMatchingHighestRatedDriverStrategy implements DriverMatchingStrategy {
    @Override
    public List<Driver> findMatchingDrivers(RideRequest rideRequest) {
        return List.of();
    }
}
