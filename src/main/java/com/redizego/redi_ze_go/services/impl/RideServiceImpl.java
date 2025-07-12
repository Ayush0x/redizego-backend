package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.repositories.RideRepository;
import com.redizego.redi_ze_go.services.RideRequestService;
import com.redizego.redi_ze_go.services.RideService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final RideRequestService rideRequestService;
    private final ModelMapper modelMapper;

    @Override
    public Ride getRideById(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(()->
                        new RuntimeException("Ride not found with id "+ id));
    }

    @Override
    public Ride createNewRide(RideRequest rideRequest, Driver driver) {
        rideRequest.setRideRequestStatus(RideRequestStatus.ACCEPTED);

        Ride ride=modelMapper.map(rideRequest,Ride.class);
        ride.setRideStatus(RideStatus.CONFIRMED);
        ride.setDriver(driver);
        ride.setOtp(generateOtp());
        ride.setId(null);

        rideRequestService.update(rideRequest);
        return rideRepository.save(ride);
    }

    @Override
    public Ride updateRideStatus(Ride ride, RideStatus rideStatus) {
        ride.setRideStatus(rideStatus);
        return rideRepository.save(ride);
    }

    @Override
    public Page<Ride> getAllRidesOfRider(Rider rider, PageRequest pageRequest) {
        return rideRepository.findByRider(rider,pageRequest);
    }

    @Override
    public Page<Ride> getAllRidesOfDriver(Driver driver, PageRequest pageRequest) {
        return rideRepository.findByDriver(driver,pageRequest);
    }

    private String generateOtp() {
        Random random=new Random();
        int otp=random.nextInt(1000000);
        return String.format("%06d", otp);
    }
}
