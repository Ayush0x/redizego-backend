package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
//import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.repositories.DriverRepository;
import com.redizego.redi_ze_go.services.DriverService;
import com.redizego.redi_ze_go.services.PaymentService;
import com.redizego.redi_ze_go.services.RideRequestService;
import com.redizego.redi_ze_go.services.RideService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final RideRequestService rideRequestService;
    private final DriverRepository driverRepository;
    private final RideService rideService;
    private final ModelMapper modelMapper;
    private final PaymentService paymentService;

    @Override
    public RideDto cancelRide(Long rideId) {
        Ride ride=rideService.getRideById(rideId);
        Driver driver=getCurrentDriver();

        if(!driver.equals(ride.getDriver())){
            throw new RuntimeException("Driver cannot cancel this ride as he has not accepted the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.CONFIRMED)){
            throw new RuntimeException("Ride cannot be cancelled as the ride status is not invalid "+ride.getRideStatus());
        }

        rideService.updateRideStatus(ride,RideStatus.CANCELLED);
        updateDriverAvailability(driver,true);

        return modelMapper.map(ride,RideDto.class);
    }

    @Override
    public RideDto startRide(Long rideId,String otp) {
        Ride ride=rideService.getRideById(rideId);
        Driver driver=getCurrentDriver();

        if(!driver.equals(ride.getDriver())){
            throw new RuntimeException("Driver cannot start this ride as he has not accepted the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.CONFIRMED)){
            throw new RuntimeException("Driver cannot start this ride as the ride status is not CONFIRMED "+ride.getRideStatus());
        }

        if(!otp.equals(ride.getOtp())){
            throw new RuntimeException("Driver cannot start this ride as the otp is not valid");
        }

        ride.setStartTime(LocalDateTime.now());
        Ride updatedRide=rideService.updateRideStatus(ride,RideStatus.ONGOING);

        paymentService.createNewPayment(updatedRide);

        return modelMapper.map(updatedRide,RideDto.class);
    }

    @Override
    @Transactional
    public RideDto endRide(Long rideId) {
        Ride ride=rideService.getRideById(rideId);
        Driver driver=getCurrentDriver();

        if(!driver.equals(ride.getDriver())){
            throw new RuntimeException("Driver cannot end this ride as he has not accepted the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.ONGOING)){
            throw new RuntimeException("Driver cannot end this ride as the ride status is not ONGOING "+ride.getRideStatus());
        }

        ride.setEndTime(LocalDateTime.now());
        rideService.updateRideStatus(ride,RideStatus.ENDED);
        updateDriverAvailability(driver,true);

        paymentService.processPayment(ride);

        return modelMapper.map(ride,RideDto.class);
    }

    @Override
    public RiderDto rateRider(Long riderId, Integer rating) {
        return null;
    }

    @Override
    @Transactional
    public RideDto acceptRide(Long rideId) {
        RideRequest rideRequest=rideRequestService.findRideRequestById(rideId);

        if(!rideRequest.getRideRequestStatus().equals(RideRequestStatus.PENDING)){
            throw new RuntimeException("Ride request is cannot be accepted, statue is "+rideRequest.getRideRequestStatus());
        }

        Driver driver=getCurrentDriver();

        if(!driver.getIsAvailable()){
            throw  new RuntimeException("Driver is not available");
        }

        Driver savedDriver=updateDriverAvailability(driver,false);
        Ride ride=rideService.createNewRide(rideRequest,savedDriver);

        return modelMapper.map(ride,RideDto.class);
    }

    @Override
    public DriverDto getMyProfile() {
        Driver driver=getCurrentDriver();

        return modelMapper.map(driver,DriverDto.class);
    }

    @Override
    public Page<RideDto> getAllRides(PageRequest pageRequest) {
        Driver driver=getCurrentDriver();

        return rideService.getAllRidesOfDriver(driver,pageRequest).map(
                ride->modelMapper.map(ride,RideDto.class)
        );
    }

    @Override
    public Driver getCurrentDriver() {
        return driverRepository.findById(2L)
                .orElseThrow(()->
                        new RuntimeException("Driver not found with id "+ 2L));
    }

    @Override
    public Driver updateDriverAvailability(Driver driver, boolean isAvailable) {
        driver.setIsAvailable(isAvailable);

        return driverRepository.save(driver);
    }

    @Override
    public Driver createNewDriver(Driver driver) {
        return driverRepository.save(driver);
    }
}
