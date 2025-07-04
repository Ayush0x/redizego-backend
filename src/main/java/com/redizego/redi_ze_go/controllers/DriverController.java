package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.RideStartDto;
import com.redizego.redi_ze_go.services.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/accept-ride/{ride-request-id}")
    public ResponseEntity<RideDto> acceptRide(@PathVariable("ride-request-id") Long rideRequestId){
        return ResponseEntity.ok(driverService.acceptRide(rideRequestId));
    }

    @PostMapping("/start-ride/{ride-request-id}")
    public ResponseEntity<RideDto> startRide(@PathVariable("ride-request-id") Long rideId,@RequestBody RideStartDto otp){
        return ResponseEntity.ok(driverService.startRide(rideId,otp.getOtp()));
    }
}
