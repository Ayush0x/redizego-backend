package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.PointDto;
import com.redizego.redi_ze_go.dtos.RideRequestDto;
import com.redizego.redi_ze_go.entities.RetryRequest;
import com.redizego.redi_ze_go.entities.enums.RetryStatus;
import com.redizego.redi_ze_go.repositories.RetryRequestRepository;
import com.redizego.redi_ze_go.services.RetryScheduledService;
import com.redizego.redi_ze_go.services.RiderService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetryScheduledServiceImpl implements RetryScheduledService {

    private final RetryRequestRepository retryRequestRepository;
    private final RiderService riderService;

    @Scheduled(fixedDelay = 5000) // Retry every 5 seconds
    @Override
    public void retryPendingRequests() {
        List<RetryRequest> retryRequestList=retryRequestRepository.findByRetryStatus(RetryStatus.PENDING);

        for(RetryRequest retryRequest:retryRequestList){
            try{
                Point pickup = retryRequest.getPickupLocation();
                double[] coords = new double[] { pickup.getY(), pickup.getX() };
                RideRequestDto rideRequestDto=new RideRequestDto();
                rideRequestDto.setPickupLocation(new PointDto(coords));
                Point dropoff = retryRequest.getDestinationLocation();
                double[] coords1 = new double[] { dropoff.getY(), dropoff.getX() };
                rideRequestDto.setDestinationLocation(new PointDto(coords1));
                rideRequestDto.getRider();

                riderService.handleOnlineRideRequest(rideRequestDto);
                retryRequest.setRetryStatus(RetryStatus.SUCCESS);
            }
            catch (Exception e){
                retryRequest.setRetryCount(retryRequest.getRetryCount()+1);
                if(retryRequest.getRetryCount() >= 3) {
                    retryRequest.setRetryStatus(RetryStatus.FAILED);
                }
            }
            retryRequestRepository.save(retryRequest);
        }
    }
}
