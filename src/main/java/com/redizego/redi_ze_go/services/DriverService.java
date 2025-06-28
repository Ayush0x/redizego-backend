package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RiderDto;

import java.util.List;

public interface DriverService {

    RideDto cancelRide(Long rideId);

    RideDto startRide(Long rideId);

    RideDto endRide(Long rideId);

    RiderDto rateRider(Long riderId, Integer rating);

    RideDto acceptRide(Long rideId);

    DriverDto getMyProfile();

    List<RideDto> getAllRides();

}
