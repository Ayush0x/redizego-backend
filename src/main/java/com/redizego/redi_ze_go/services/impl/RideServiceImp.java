package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.services.RideService;
import com.redizego.redi_ze_go.services.RiderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class RideServiceImp implements RideService {
    @Override
    public Ride getRideById(Long id) {
        return null;
    }

    @Override
    public void matchWithDriver(RideRequestDto rideRequestDto) {

    }

    @Override
    public Ride createRide(RideRequestDto rideRequestDto, Driver driver) {
        return null;
    }

    @Override
    public Ride updateRide(Long id, RideStatus rideStatus) {
        return null;
    }

    @Override
    public Page<Ride> getAlRidesOfRider(Long riderId, PageRequest pageRequest) {
        return null;
    }

    @Override
    public Page<Ride> getAlRidesOfDriver(Long driverId, PageRequest pageRequest) {
        return null;
    }
}
