package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface RideService {

    Ride getRideById(Long id);

    void matchWithDriver(RideRequestDto rideRequestDto);

    Ride createRide(RideRequestDto rideRequestDto, Driver driver);

    Ride updateRide(Long id, RideStatus rideStatus);

    Page<Ride> getAlRidesOfRider(Long riderId, PageRequest pageRequest);

    Page<Ride> getAlRidesOfDriver(Long driverId, PageRequest pageRequest);


}
