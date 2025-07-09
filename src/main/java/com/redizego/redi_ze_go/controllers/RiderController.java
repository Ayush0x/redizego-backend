package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.services.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rider")
@RequiredArgsConstructor
//@AllArgsConstructor
public class RiderController {

    private final RiderService riderService;

    @PostMapping("/request-ride")
    public ResponseEntity<RideRequestDto> requestRide(@RequestBody RideRequestDto rideRequestDto) {
        return ResponseEntity.ok().body(riderService.requestRide(rideRequestDto));
    }

    @PostMapping("/cancel-ride/{ride-id}")
    public ResponseEntity<RideDto> cancelRide(@PathVariable("ride-id") Long rideId) {
        return ResponseEntity.ok().body(riderService.cancelRide(rideId));
    }

    @PostMapping("/rate-driver")
    public ResponseEntity<DriverDto> rateDriver(@RequestBody RatingDto ratingDto) {
        return ResponseEntity.ok().body(riderService.rateDriver(ratingDto.getRideId(), ratingDto.getRating()));
    }

    @GetMapping("/my-profile")
    public ResponseEntity<RiderDto> getMyProfile() {
        return ResponseEntity.ok().body(riderService.getMyProfile());
    }

    @GetMapping("/all-rides")
    public ResponseEntity<Page<RideDto>> getAllRides(@RequestParam(defaultValue = "0") Integer pageOffset,
                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageSize, Sort.by
                (Sort.Direction.DESC,"createdTime","id"));
        return ResponseEntity.ok().body(riderService.getAllRides(pageRequest));
    }
}
