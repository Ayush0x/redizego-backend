package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.entities.*;
import com.redizego.redi_ze_go.entities.enums.PaymentMethods;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.repositories.RideRequestRepository;
import com.redizego.redi_ze_go.repositories.RiderRepository;
import com.redizego.redi_ze_go.services.DriverService;
import com.redizego.redi_ze_go.services.RatingService;
import com.redizego.redi_ze_go.services.RideService;
import com.redizego.redi_ze_go.services.RiderService;
import com.redizego.redi_ze_go.strategies.RideStrategyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderServiceImpl implements RiderService {

    private final ModelMapper modelMapper;
    private final RideStrategyManager rideStrategyManager;
    private final RideRequestRepository rideRequestRepository;
    private final RiderRepository riderRepository;
    private final RideService rideService;
    private final DriverService driverService;
    private final RatingService ratingService;

    private final Double DOWNLOAD_SPEED_THRESHOLD = 256.0; // in Kbps
    private final Double UPLOAD_SPEED_THRESHOLD = 512.0; // in Kbps

    @Override
    @Transactional
    public RideRequestDto requestRide(RideRequestDto rideRequestDto) {
        if(rideStrategyManager.networkStrategy().isNetworkWeak(DOWNLOAD_SPEED_THRESHOLD, UPLOAD_SPEED_THRESHOLD)) {
            log.error("Network is weak, swwitching to offline mode");
            return handleOfflineRideRequest(rideRequestDto);
        }
        else {
            log.info("Network is strong, switching to online mode");
            return handleOnlineRideRequest(rideRequestDto);
        }
    }

    @Override
    public RideDto cancelRide(Long rideId) {
        Rider rider=getCurrentRider();
        Ride ride=rideService.getRideById(rideId);

        if(!rider.equals(ride.getRider())){
            throw new RuntimeException("Rider cannot cancel this ride as he has not requested the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.CONFIRMED)){
            throw new RuntimeException("Ride cannot be cancelled as the ride status is not invalid "+ride.getRideStatus());
        }

        Ride savedRide=rideService.updateRideStatus(ride,RideStatus.CANCELLED);

        driverService.updateDriverAvailability(ride.getDriver(),true);

        return modelMapper.map(savedRide,RideDto.class);
    }

    @Override
    public DriverDto rateDriver(Long driverId, Integer rating) {
        Ride ride=rideService.getRideById(driverId);
        Rider rider=getCurrentRider();

        if(!rider.equals(ride.getRider())) {
            throw new RuntimeException("Rider cannot rate this driver as he has not requested the request");
        }

        if(!ride.getRideStatus().equals(RideStatus.ENDED)){
            throw new RuntimeException("Rider cannot rate this driver as the ride status is not ENDED "+ride.getRideStatus());
        }

        return ratingService.rateDriver(ride,rating);
    }

    @Override
    public RiderDto getMyProfile() {
        Rider rider=getCurrentRider();
        return modelMapper.map(rider,RiderDto.class);
    }

    @Override
    public Page<RideDto> getAllRides(PageRequest pageRequest) {
        Rider rider=getCurrentRider();


        return rideService.getAllRidesOfRider(rider,pageRequest).map(
                ride->modelMapper.map(ride,RideDto.class)
        );
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

    public Rider getCurrentRider(){
        User  user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return riderRepository.findByUser(user)
                .orElseThrow(()->
                        new ResourceNotFoundException("Rider not found with id "+ user.getId()));
    }

    @Override
    public RideRequestDto handleOnlineRideRequest(RideRequestDto rideRequestDto) {
        Rider rider=getCurrentRider();
        RideRequest rideRequest=modelMapper.map(rideRequestDto,RideRequest.class);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        rideRequest.setRider(rider);

        Double fare=rideStrategyManager.rideFareCalculationStrategy().calculateFare(rideRequest);
        rideRequest.setFare(fare);

        RideRequest savedRideRequest=rideRequestRepository.save(rideRequest);

        List<Driver> drivers=rideStrategyManager
                .driverMatchingStrategy(rider.getRating()).findMatchingDrivers(rideRequest);

        return modelMapper.map(savedRideRequest,RideRequestDto.class);
    }

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5) // Retry with exponential backoff
    )
    @Override
    public RideRequestDto handleOfflineRideRequest(RideRequestDto rideRequestDto) {
        log.info("Handling offline ride request");

        PointDto pickupLocation = rideRequestDto.getPickupLocation();
        PointDto dropLocation = rideRequestDto.getDestinationLocation();
        if(pickupLocation==null || dropLocation==null){
            throw new RuntimeException("Pickup and drop locations cannot be null for offline ride request");
        }

        if(pickupLocation==dropLocation){
            throw new RuntimeException("Pickup and drop locations cannot be same for offline ride request");
        }

        log.info("Pickup Location: {}, Drop Location: {}", pickupLocation, dropLocation);

        Rider rider = getCurrentRider();
        RideRequest rideRequest = modelMapper.map(rideRequestDto, RideRequest.class);

        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        rideRequest.setRider(rider);

        Double fare = rideStrategyManager.rideFareCalculationStrategy().calculateFare(rideRequest);
        rideRequest.setFare(fare);
        rideRequest.setPaymentMethod(PaymentMethods.CASH);

        RideRequest savedRideRequest = rideRequestRepository.save(rideRequest);

        return modelMapper.map(savedRideRequest, RideRequestDto.class);
    }
}
