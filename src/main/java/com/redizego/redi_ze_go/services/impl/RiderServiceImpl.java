package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.RideRequest;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.repositories.RideRequestRepository;
import com.redizego.redi_ze_go.repositories.RiderRepository;
import com.redizego.redi_ze_go.services.RiderService;
import com.redizego.redi_ze_go.strategies.DriverMatchingStrategy;
import com.redizego.redi_ze_go.strategies.RideFareCalculationStrategy;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderServiceImpl implements RiderService {

    private final ModelMapper modelMapper;

//    @Qualifier("rideFareDefaultFareCalculationStrategy")
    private final RideFareCalculationStrategy rideFareCalculationStrategy;

//    @Qualifier("driverMatchingNearestDriverStrategy")
    private final DriverMatchingStrategy driverMatchingStrategy;

    private final RideRequestRepository rideRequestRepository;
    private final RiderRepository riderRepository;

    @Override
    public RideRequestDto requestRide(RideRequestDto rideRequestDto) {
        RideRequest rideRequest=modelMapper.map(rideRequestDto,RideRequest.class);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);

        Double fare=rideFareCalculationStrategy.calculateFare(rideRequest);
        rideRequest.setFare(fare);

        RideRequest savedRideRequest=rideRequestRepository.save(rideRequest);

        driverMatchingStrategy.findMatchingDrivers(rideRequest);

        return modelMapper.map(savedRideRequest,RideRequestDto.class);
    }

    @Override
    public RideDto cancelRide(Long rideId) {
        return null;
    }

    @Override
    public DriverDto rateDriver(Long driverId, Integer rating) {
        return null;
    }

    @Override
    public RiderDto getMyProfile() {
        return null;
    }

    @Override
    public List<RideDto> getAllRides() {
        return List.of();
    }

    @Override
    public Rider createNewRider(User user) {
        Rider rider=Rider
                .builder()
                .user(user)
                .rating(0.0)
                .build();
        return riderRepository.save(rider);
    }
}
