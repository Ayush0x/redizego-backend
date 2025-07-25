package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RideStartDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.services.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    private RiderDto riderDto;
    private RideDto rideDto;
    private DriverDto driverDto;

    @BeforeEach
    void setUp() {
        riderDto = new RiderDto();
        driverDto = new DriverDto();
        rideDto = new RideDto();
    }

    @Test
    void testAcceptRide_ShouldAcceptRide() {
        when(driverService.acceptRide(1L)).thenReturn(rideDto);

        ResponseEntity<RideDto> response = driverController.acceptRide(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rideDto, response.getBody());
    }

    @Test
    void testStartRide_ShouldStartRide() {
        RideStartDto rideStartDto = new RideStartDto();
        rideStartDto.setOtp("123456");

        when(driverService.startRide(1L, "123456")).thenReturn(rideDto);

        ResponseEntity<RideDto> response = driverController.startRide(1L, rideStartDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rideDto, response.getBody());
    }

    @Test
    void testEndRide_shouldEndRide() {
        when(driverService.endRide(1L)).thenReturn(rideDto);

        ResponseEntity<RideDto> response = driverController.endRide(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rideDto, response.getBody());
    }

    @Test
    void testcancelRide_shouldCancelRide() {
        when(driverService.cancelRide(1L)).thenReturn(rideDto);

        ResponseEntity<RideDto> response = driverController.cancelRide(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rideDto, response.getBody());
    }

    @Test
    void testRateRider_shouldRateRider() {
        when(driverService.rateRider(1L, 4)).thenReturn(riderDto);

        ResponseEntity<RiderDto> response = driverController.rateRider(1L, 4);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(riderDto, response.getBody());
    }

    @Test
    void testGetMyProfile_shouldGetMyProfile() {
        when(driverService.getMyProfile()).thenReturn(driverDto);

        ResponseEntity<DriverDto> response = driverController.getMyProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(driverDto, response.getBody());
    }
    
    @Test
    void testGetMyRides_shouldGetMyRides() {
        PageRequest page=PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"createdTime","id"));
        Page<RideDto> rides=new PageImpl<>(Collections.singletonList(rideDto));

        when(driverService.getAllRides(page)).thenReturn(rides);

        ResponseEntity<Page<RideDto>> response = driverController.getAllRides(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rides, response.getBody());
    }
}
