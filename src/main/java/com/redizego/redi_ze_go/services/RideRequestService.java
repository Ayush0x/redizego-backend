package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.entities.RideRequest;

public interface RideRequestService {

    RideRequest findRideRequestById(Long rideRequestId);

    void update(RideRequest rideRequest);
}
