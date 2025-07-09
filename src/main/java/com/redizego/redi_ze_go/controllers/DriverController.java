package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.services.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @PostMapping("/end-ride/{ride-id}")
    public ResponseEntity<RideDto> endRide(@PathVariable("ride-id") Long rideId){
        return ResponseEntity.ok(driverService.endRide(rideId));
    }

    @PostMapping("/cancel-ride/{ride-id}")
    public ResponseEntity<RideDto> cancelRide(@PathVariable("ride-id") Long rideId) {
        return ResponseEntity.ok().body(driverService.cancelRide(rideId));
    }

    @PostMapping("/rate-rider/{rider-id}")
    public ResponseEntity<RiderDto> rateRider(@RequestBody RatingDto ratingDto){
        return ResponseEntity.ok().body(driverService.rateRider(ratingDto.getRideId(),ratingDto.getRating()));
    }

    @GetMapping("/my-profile")
    public ResponseEntity<DriverDto> getMyProfile(){
        return ResponseEntity.ok().body(driverService.getMyProfile());
    }

    @GetMapping("/all-rides")
    public ResponseEntity<Page<RideDto>> getAllRides(@RequestParam(defaultValue = "0") Integer pageOffset,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageSize, Sort.by
                (Sort.Direction.DESC,"createdTime","id"));
        return ResponseEntity.ok().body(driverService.getAllRides(pageRequest));
    }
}
