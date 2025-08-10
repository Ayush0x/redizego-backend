package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Rating;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.repositories.DriverRepository;
import com.redizego.redi_ze_go.repositories.RatingRepository;
import com.redizego.redi_ze_go.repositories.RiderRepository;
import com.redizego.redi_ze_go.services.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Ride ride;
    private Driver driver;
    private Rider rider;
    private Rating rating;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId(1L);
        driver.setRating(4.5);

        rider = new Rider();
        rider.setId(1L);
        rider.setRating(4.5);

        ride = new Ride();
        ride.setId(1L);
        ride.setDriver(driver);
        ride.setRider(rider);

        rating = new Rating();
        rating.setId(1L);
        rating.setDriver(driver);
        rating.setRider(rider);
        rating.setRide(ride);
    }

    @Test
    void createNewRating_shouldSaveNewRating() {
        ratingService.createNewRating(ride);

        ArgumentCaptor<Rating> ratingArgumentCaptor=ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(ratingArgumentCaptor.capture());
        Rating savedRating=ratingArgumentCaptor.getValue();

        assertEquals(ride, savedRating.getRide());
        assertEquals(driver, savedRating.getDriver());
        assertEquals(rider, savedRating.getRider());
    }

    @Test
    void rateDriver_withValidRating_ShouldUpdateDriverRating() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(modelMapper.map(driver, DriverDto.class)).thenReturn(new DriverDto());

        ratingService.rateDriver(ride, 5);

        verify(ratingRepository).save(rating);
        verify(driverRepository).save(any(Driver.class));
        assertEquals(5.0, Double.valueOf(rating.getDriverRating()));
    }

    @Test
    void rateDriver_WithValidRating_ShouldUpdateDriverRating() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(modelMapper.map(driver, DriverDto.class)).thenReturn(new DriverDto());

        ratingService.rateDriver(ride, 5);

        verify(ratingRepository).save(rating);
        verify(driverRepository).save(any(Driver.class));
        assertEquals(5, rating.getDriverRating());
    }

    @Test
    void rateDriver_WhenRideNotFound_ShouldThrowException() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> ratingService.rateDriver(ride, 5));

        assertEquals("Cannot rate as the ride does not exist", exception.getMessage());
    }

    @Test
    void rateDriver_WhenAlreadyRated_ShouldThrowException() {
        rating.setDriverRating(4);
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));

        Exception exception = assertThrows(RuntimeException.class,
                () -> ratingService.rateDriver(ride, 5));

        assertEquals("Driver has already been rated by this rider", exception.getMessage());
    }

    @Test
    void rateRider_WithValidRating_ShouldUpdateRiderRating() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));
        when(riderRepository.save(any(Rider.class))).thenReturn(rider);
        when(modelMapper.map(rider, RiderDto.class)).thenReturn(new RiderDto());

        ratingService.rateRider(ride, 5);

        verify(ratingRepository).save(rating);
        verify(riderRepository).save(any(Rider.class));
        assertEquals(5, rating.getRiderRating());
    }

    @Test
    void rateRider_WhenRideNotFound_ShouldThrowException() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> ratingService.rateRider(ride, 5));

        assertEquals("Cannot rate as the ride does not exist", exception.getMessage());
    }

    @Test
    void rateRider_WhenAlreadyRated_ShouldThrowException() {
        rating.setRiderRating(4);
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));

        Exception exception = assertThrows(RuntimeException.class,
                () -> ratingService.rateRider(ride, 5));

        assertEquals("Rider has already been rated by this driver", exception.getMessage());
    }

}
