package com.redizego.redi_ze_go.dtos;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.enums.PaymentMethods;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideDto {

    private Long id;

    private Point pickupLocation;

    private Point dropOffLocation;

    private LocalDateTime createdTime;

    private Rider rider;

    private Driver driver;

    private PaymentMethods paymentMethod;

    private RideStatus  rideStatus;

    private Double fare;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
