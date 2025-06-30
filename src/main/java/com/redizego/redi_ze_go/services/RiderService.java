package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface RiderService {

    RideRequestDto requestRide(RideRequestDto rideRequestDto);

    RideDto cancelRide(Long rideId);

    DriverDto rateDriver(Long driverId, Integer rating);

    RiderDto getMyProfile();

    List<RideDto> getAllRides();

    Rider createNewRider(User user);
}
