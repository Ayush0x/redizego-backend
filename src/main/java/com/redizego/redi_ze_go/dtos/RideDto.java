package com.redizego.redi_ze_go.dtos;

import com.redizego.redi_ze_go.entities.enums.PaymentMethods;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideDto {

    private Long id;

    private PointDto pickupLocation;

    private PointDto destinationLocation;

    private LocalDateTime requestTime;

    private RiderDto rider;

    private DriverDto driver;

    private PaymentMethods paymentMethod;

    private RideStatus  rideStatus;

    private String otp;

    private Double fare;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
