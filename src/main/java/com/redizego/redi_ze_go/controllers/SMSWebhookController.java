package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.PointDto;
import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.services.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/sms-webhook")
@RequiredArgsConstructor
public class SMSWebhookController {

    private final BeanDefinitionRegistryPostProcessor springSecurityPathPatternParserBeanDefinitionRegistryPostProcessor;
    private final RiderService riderService;

    @PostMapping("/request-offline-ride")
    public ResponseEntity<RideRequestDto> requestOfflineRide(@RequestParam(value = "From",required = false)String from,@RequestParam(value = "Body",required = false)String body) {
        RideRequestDto rideRequestDto= new RideRequestDto();
//        rideRequestDto.setPickupLocation(parsePickup(body));
        double [] pickupCoordinates = parseCoordinates(parsePickup(body));
        rideRequestDto.setPickupLocation(new PointDto(pickupCoordinates));
        double [] dropCoordinates=parseCoordinates(parseDrop(body));
        rideRequestDto.setDestinationLocation(new PointDto(dropCoordinates));

        RideRequestDto processedRequest = riderService.handleOfflineRideRequest(rideRequestDto);
        return ResponseEntity.ok().body(processedRequest);
    }

    private String parsePickup(String body) {
        int start = body.toLowerCase().indexOf("pickup:");
        if (start == -1) return "";
        int end = body.toLowerCase().indexOf("drop:");
        return body.substring(start + 7, (end == -1 ? body.length() : end)).trim();
    }

    private String parseDrop(String body) {
        int start = body.toLowerCase().indexOf("drop:");
        if (start == -1) return "";
        return body.substring(start + 5).trim();
    }

    private double[] parseCoordinates(String location) {
        // Expects location in "lat,lng" format
        String[] parts = location.split(",");
        if (parts.length != 2) return new double[]{0.0, 0.0};
        return new double[]{Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim())};
    }

}
