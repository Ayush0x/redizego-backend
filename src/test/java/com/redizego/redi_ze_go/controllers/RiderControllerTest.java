package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.dtos.RiderDto;
import com.redizego.redi_ze_go.services.RiderService;
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
public class RiderControllerTest {

    @Mock
    private RiderService riderService;

    @InjectMocks
    private RiderController riderController;

    private RideRequestDto rideRequestDto;
    private RideDto rideDto;
    private RiderDto riderDto;
    private DriverDto driverDto;

    @BeforeEach
    void setUp() {
        riderDto = new RiderDto();
        rideDto = new RideDto();
        rideRequestDto = new RideRequestDto();
    }

    @Test
    void testRequestRide_shouldRequestRide() {
        when(riderService.requestRide(rideRequestDto)).thenReturn(rideRequestDto);

        ResponseEntity<RideRequestDto> response = riderController.requestRide(rideRequestDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rideRequestDto, response.getBody());
    }

    @Test
    void testCancelRide_shouldCancelRide() {
        when(riderService.cancelRide(1L)).thenReturn(rideDto);

        ResponseEntity<RideDto> response = riderController.cancelRide(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rideDto, response.getBody());
    }

    @Test
    void testMyProfile_shouldGetMyProfile() {
        when(riderService.getMyProfile()).thenReturn(riderDto);

        ResponseEntity<RiderDto> response = riderController.getMyProfile();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(riderDto, response.getBody());
    }

    @Test
    void testGetAllRides_shouldGetAllRides() {
        PageRequest page= PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"createdTime","id"));
        Page<RideDto> rides=new PageImpl<>(Collections.singletonList(rideDto));

        when(riderService.getAllRides(page)).thenReturn(rides);

        ResponseEntity<Page<RideDto>> response = riderController.getAllRides(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rides, response.getBody());
    }

    @Test
    void testRateDriver_shouldRateDriver() {
        when(riderService.rateDriver(1L, 4)).thenReturn(driverDto);

        ResponseEntity<DriverDto> response = riderController.rateDriver(1L, 4);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(driverDto, response.getBody());
    }
}
