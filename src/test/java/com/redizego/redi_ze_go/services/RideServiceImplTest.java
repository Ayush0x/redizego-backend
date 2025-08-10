package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.entities.*;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.repositories.RideRepository;
import com.redizego.redi_ze_go.services.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RideRequestService rideRequestService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride ride;
    private RideRequest rideRequest;
    private Driver driver;
    private Rider rider;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test data
        driver = new Driver();
        driver.setId(1L);
        driver.setIsAvailable(true);

        rider = new Rider();
        rider.setId(1L);
        rider.setRating(4.5);

        rideRequest = new RideRequest();
        rideRequest.setId(1L);
        rideRequest.setRider(rider);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);

        ride = new Ride();
        ride.setId(1L);
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.CONFIRMED);
        ride.setOtp("123456");
        ride.setStartTime(LocalDateTime.now());
        
        pageable = PageRequest.of(0, 10, Sort.by("id").descending());
    }

    @Test
    void getRideById_WhenRideExists_ShouldReturnRide() {
        // Arrange
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        // Act
        Ride result = rideService.getRideById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(rideRepository, times(1)).findById(1L);
    }

    @Test
    void getRideById_WhenRideNotExists_ShouldThrowException() {
        // Arrange
        when(rideRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            rideService.getRideById(1L);
        });

        assertEquals("Ride not found with id 1", exception.getMessage());
        verify(rideRepository, times(1)).findById(1L);
    }

    @Test
    void createNewRide_ShouldCreateNewRide() {
        // Arrange
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        
        // Mock ModelMapper to return a new Ride
        Ride mappedRide = new Ride();
        when(modelMapper.map(rideRequest, Ride.class)).thenReturn(mappedRide);
        
        // Mock repository save
        Ride savedRide = new Ride();
        savedRide.setId(1L);
        savedRide.setRider(rider);
        savedRide.setDriver(driver);
        savedRide.setRideStatus(RideStatus.CONFIRMED);
        savedRide.setOtp("123456");
        
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);
        doNothing().when(rideRequestService).update(any(RideRequest.class));

        // Act
        Ride result = rideService.createNewRide(rideRequest, driver);

        // Assert
        assertNotNull(result);
        assertEquals(RideStatus.CONFIRMED, result.getRideStatus());
        assertNotNull(result.getOtp());
        assertEquals(6, result.getOtp().length());
        verify(rideRequestService).update(rideRequest);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void updateRideStatus_ShouldUpdateRideStatus() {
        // Arrange
        RideStatus newStatus = RideStatus.ONGOING;
        Ride updatedRide = new Ride();
        updatedRide.setId(1L);
        updatedRide.setRideStatus(newStatus);
        
        when(rideRepository.save(any(Ride.class))).thenReturn(updatedRide);

        // Act
        Ride result = rideService.updateRideStatus(ride, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getRideStatus());
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    void getAllRidesOfRider_ShouldReturnRiderRides() {
        // Arrange
        Page<Ride> ridePage = new PageImpl<>(Collections.singletonList(ride), pageable, 1);
        when(rideRepository.findByRider(eq(rider), any(Pageable.class))).thenReturn(ridePage);

        // Act
        Page<Ride> result = rideService.getAllRidesOfRider(rider, (PageRequest) pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(rideRepository, times(1)).findByRider(eq(rider), any(Pageable.class));
    }

    @Test
    void getAllRidesOfDriver_ShouldReturnDriverRides() {
        // Arrange
        Page<Ride> ridePage = new PageImpl<>(Collections.singletonList(ride), pageable, 1);
        when(rideRepository.findByDriver(eq(driver), any(Pageable.class))).thenReturn(ridePage);

        // Act
        Page<Ride> result = rideService.getAllRidesOfDriver(driver, (PageRequest) pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(rideRepository, times(1)).findByDriver(eq(driver), any(Pageable.class));
    }

    @Test
    void updateRideStatus_WithNullRide_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            rideService.updateRideStatus(null, RideStatus.ENDED);
        });
    }

    @Test
    void getAllRidesOfRider_WithNoRides_ShouldReturnEmptyPage() {
        // Arrange
        Page<Ride> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(rideRepository.findByRider(eq(rider), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Ride> result = rideService.getAllRidesOfRider(rider, (PageRequest) pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
