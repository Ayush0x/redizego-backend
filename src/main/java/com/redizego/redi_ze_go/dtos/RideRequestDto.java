package com.redizego.redi_ze_go.dtos;

import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.enums.PaymentMethods;
import com.redizego.redi_ze_go.entities.enums.RideRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDto {

    private Long id;

    private PointDto pickupLocation;

    private PointDto destinationLocation;

    private LocalDateTime requestTime;

    private RiderDto rider;

    private PaymentMethods paymentMethod;

    private RideRequestStatus rideRequestStatus;

}
