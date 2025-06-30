package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.RideRequest;

import java.util.List;

public interface DriverMatchingStrategy {

    List<Driver> findMatchingDrivers(RideRequest rideRequest);
}
