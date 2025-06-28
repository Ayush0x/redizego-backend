package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.entities.Driver;

import java.util.List;

public interface DriverMatchigStrategy {

    List<Driver> findMatchingDrivers(RideRequestDto rideRequestDto);
}
