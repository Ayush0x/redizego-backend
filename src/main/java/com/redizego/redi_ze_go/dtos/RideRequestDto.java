package com.redizego.redi_ze_go.dtos;

import com.redizego.redi_ze_go.entities.enums.PaymentMethods;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDto {

    private Long id;

    private PointDto pickupLocation;

    private PointDto destinationLocation;

    private PaymentMethods paymentMethod;

    private LocalDateTime requestTime;

    private RiderDto rider;

    private Double fare;

    private RideRequestStatus rideRequestStatus;

}
