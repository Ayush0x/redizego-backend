package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Rating;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.repositories.DriverRepository;
import com.redizego.redi_ze_go.repositories.RatingRepository;
import com.redizego.redi_ze_go.repositories.RiderRepository;
import com.redizego.redi_ze_go.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;
    private final RiderRepository riderRepository;
    private final ModelMapper modelMapper;

    @Override
    public DriverDto rateDriver(Ride ride, Integer rating) {
        Driver driver=ride.getDriver();
        Rating ratingObject=ratingRepository.findByRide(ride)
                .orElseThrow(
                        () -> new RuntimeException("Cannot rate as the ride does not exist"));

        if(ratingObject.getDriverRating()!=null){
            throw new RuntimeException("Driver has already been rated by this rider");
        }

        ratingObject.setDriverRating(rating);
        ratingRepository.save(ratingObject);

        Double newRating=ratingRepository.findByDriver(driver)
                .stream().mapToDouble(Rating::getDriverRating)
                .average()
                .orElse(0.0);

        driver.setRating(newRating);
        Driver savedDriver=driverRepository.save(driver);

        return modelMapper.map(savedDriver, DriverDto.class);
    }

    @Override
    public RiderDto rateRider(Ride ride, Integer rating) {
        Rider rider=ride.getRider();
        Rating ratingObject=ratingRepository.findByRide(ride)
                .orElseThrow(
                        () -> new RuntimeException("Cannot rate as the ride does not exist"));

        if(ratingObject.getRiderRating()!=null){
            throw new RuntimeException("Rider has already been rated by this driver");
        }

        ratingObject.setRiderRating(rating);
        ratingRepository.save(ratingObject);

        Double newRating=ratingRepository.findByRider(rider)
                .stream().mapToDouble(Rating::getRiderRating)
                .average()
                .orElse(0.0);

        rider.setRating(newRating);
        Rider savedRider=riderRepository.save(rider);

        return modelMapper.map(savedRider, RiderDto.class);
    }

    @Override
    public void createNewRating(Ride ride) {
        Rating rating=Rating.builder()
                .rider(ride.getRider())
                .driver(ride.getDriver())
                .ride(ride)
                .build();

        ratingRepository.save(rating);
    }
}
