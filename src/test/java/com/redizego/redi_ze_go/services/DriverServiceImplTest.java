package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.entities.*;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import com.redizego.redi_ze_go.entities.enums.Roles;
//import com.redizego.redi_ze_go.entities.enums.UserRole;
import com.redizego.redi_ze_go.repositories.DriverRepository;
import com.redizego.redi_ze_go.services.impl.DriverServiceImpl;
import com.redizego.redi_ze_go.services.impl.RideRequestServiceImpl;
import com.redizego.redi_ze_go.services.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverServiceImplTest {

    @Mock
    private RideRequestService rideRequestService;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RideService rideService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RatingService ratingService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DriverServiceImpl driverService;

    private User testUser;
    private Driver testDriver;
    private Ride testRide;
    private RideRequest testRideRequest;
    private final Long TEST_DRIVER_ID = 1L;
    private final Long TEST_RIDE_ID = 1L;
    private final Long TEST_RIDER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test.driver@example.com");
        testUser.setRole(Set.of(Roles.RIDER));

        // Setup test driver
        testDriver = new Driver();
        testDriver.setId(TEST_DRIVER_ID);
        testDriver.setUser(testUser);
        testDriver.setIsAvailable(true);

        // Setup test ride
        testRide = new Ride();
        testRide.setId(TEST_RIDE_ID);
        testRide.setDriver(testDriver);
        testRide.setRideStatus(RideStatus.CONFIRMED);
        testRide.setOtp("123456");

        // Setup test ride request
        testRideRequest = new RideRequest();
        testRideRequest.setId(1L);
        testRideRequest.setRideRequestStatus(RideRequestStatus.PENDING);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
    }

    // Test for cancelRide
    @Test
    void cancelRide_WhenValidRideAndDriver_ShouldCancelRide() {
        // Arrange
        when(rideService.getRideById(TEST_RIDE_ID)).thenReturn(testRide);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
        when(rideService.updateRideStatus(any(Ride.class), eq(RideStatus.CANCELLED))).thenReturn(testRide);
        when(modelMapper.map(testRide, RideDto.class)).thenReturn(new RideDto());

        // Act
        RideDto result = driverService.cancelRide(TEST_RIDE_ID);

        // Assert
        assertNotNull(result);
        verify(rideService).updateRideStatus(testRide, RideStatus.CANCELLED);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void cancelRide_WhenNotTheDriver_ShouldThrowException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        Driver otherDriver = new Driver();
        otherDriver.setId(2L);
        otherDriver.setUser(otherUser);
        testRide.setDriver(otherDriver);
        
        when(rideService.getRideById(TEST_RIDE_ID)).thenReturn(testRide);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> driverService.cancelRide(TEST_RIDE_ID));
        assertEquals("Driver cannot cancel this ride as he has not accepted the request", exception.getMessage());
    }

    // Test for startRide
    @Test
    void startRide_WithValidOtp_ShouldStartRide() {
        // Arrange
        testRide.setRideStatus(RideStatus.CONFIRMED);
        when(rideService.getRideById(TEST_RIDE_ID)).thenReturn(testRide);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));
        when(rideService.updateRideStatus(any(Ride.class), eq(RideStatus.ONGOING))).thenReturn(testRide);
        when(modelMapper.map(testRide, RideDto.class)).thenReturn(new RideDto());

        // Act
        RideDto result = driverService.startRide(TEST_RIDE_ID, "123456");

        // Assert
        assertNotNull(result);
        assertNotNull(testRide.getStartTime());
        verify(rideService).updateRideStatus(testRide, RideStatus.ONGOING);
        verify(paymentService).createNewPayment(testRide);
        verify(ratingService).createNewRating(testRide);
    }

    @Test
    void startRide_WithInvalidOtp_ShouldThrowException() {
        // Arrange
        when(rideService.getRideById(TEST_RIDE_ID)).thenReturn(testRide);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> driverService.startRide(TEST_RIDE_ID, "wrong_otp"));
        assertEquals("Driver cannot start this ride as the otp is not valid", exception.getMessage());
    }

    // Test for endRide
    @Test
    void endRide_WhenRideIsOngoing_ShouldEndRide() {
        // Arrange
        testRide.setRideStatus(RideStatus.ONGOING);
        when(rideService.getRideById(TEST_RIDE_ID)).thenReturn(testRide);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
        when(rideService.updateRideStatus(any(Ride.class), eq(RideStatus.ENDED))).thenReturn(testRide);
        when(modelMapper.map(testRide, RideDto.class)).thenReturn(new RideDto());

        // Act
        RideDto result = driverService.endRide(TEST_RIDE_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(testRide.getEndTime());
        verify(rideService).updateRideStatus(testRide, RideStatus.ENDED);
        verify(paymentService).processPayment(testRide);
    }

    // Test for rateRider
    @Test
    void rateRider_WhenRideIsEnded_ShouldRateRider() {
        // Arrange
        testRide.setRideStatus(RideStatus.ENDED);
        when(rideService.getRideById(TEST_RIDE_ID)).thenReturn(testRide);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));
        when(ratingService.rateRider(testRide, 5)).thenReturn(new RiderDto());

        // Act
        RiderDto result = driverService.rateRider(TEST_RIDE_ID, 5);

        // Assert
        assertNotNull(result);
        verify(ratingService).rateRider(testRide, 5);
    }

    // Test for acceptRide
    @Test
    void acceptRide_WhenDriverIsAvailable_ShouldAcceptRide() {
        // Arrange
        when(rideRequestService.findRideRequestById(TEST_RIDE_ID)).thenReturn(testRideRequest);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
        when(rideService.createNewRide(testRideRequest, testDriver)).thenReturn(testRide);
        when(modelMapper.map(testRide, RideDto.class)).thenReturn(new RideDto());

        // Act
        RideDto result = driverService.acceptRide(TEST_RIDE_ID);

        // Assert
        assertNotNull(result);
        verify(rideRequestService).findRideRequestById(TEST_RIDE_ID);
        verify(rideService).createNewRide(testRideRequest, testDriver);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void acceptRide_WhenDriverNotAvailable_ShouldThrowException() {
        // Arrange
        testDriver.setIsAvailable(false);
        when(rideRequestService.findRideRequestById(TEST_RIDE_ID)).thenReturn(testRideRequest);
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> driverService.acceptRide(TEST_RIDE_ID));
        assertEquals("Driver is not available", exception.getMessage());
    }

    // Test for getMyProfile
    @Test
    void getMyProfile_ShouldReturnDriverProfile() {
        // Arrange
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));
        when(modelMapper.map(testDriver, DriverDto.class)).thenReturn(new DriverDto());

        // Act
        DriverDto result = driverService.getMyProfile();

        // Assert
        assertNotNull(result);
        verify(driverRepository).findByUser(testUser);
    }

    // Test for getAllRides
    @Test
    void getAllRides_ShouldReturnPaginatedRides() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Ride> ridePage = new PageImpl<>(Collections.singletonList(testRide));
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));
        when(rideService.getAllRidesOfDriver(testDriver, pageRequest)).thenReturn(ridePage);
        when(modelMapper.map(testRide, RideDto.class)).thenReturn(new RideDto());

        // Act
        Page<RideDto> result = driverService.getAllRides(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(rideService).getAllRidesOfDriver(testDriver, pageRequest);
    }

    // Test for getCurrentDriver
    @Test
    void getCurrentDriver_WhenDriverExists_ShouldReturnDriver() {
        // Arrange
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.of(testDriver));

        // Act
        Driver result = driverService.getCurrentDriver();

        // Assert
        assertNotNull(result);
        assertEquals(TEST_DRIVER_ID, result.getId());
    }

    @Test
    void getCurrentDriver_WhenDriverNotFound_ShouldThrowException() {
        // Arrange
        when(driverRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> driverService.getCurrentDriver());
        assertEquals("Driver not found with id " + testUser.getId(), exception.getMessage());
    }

    // Test for updateDriverAvailability
//    @Test
    void updateDriverAvailability_ShouldUpdateAndReturnDriver() {
        // Arrange
        boolean newAvailability = false;
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Driver result = driverService.updateDriverAvailability(testDriver, newAvailability);

        // Assert
        assertNotNull(result);
        assertEquals(newAvailability, result.getIsAvailable());
        verify(driverRepository).save(testDriver);
    }

    // Test for createNewDriver
//    @Test
    void createNewDriver_ShouldSaveAndReturnDriver() {
        // Arrange
        when(driverRepository.save(testDriver)).thenReturn(testDriver);

        // Act
        Driver result = driverService.createNewDriver(testDriver);

        // Assert
        assertNotNull(result);
        verify(driverRepository).save(testDriver);
    }
}
