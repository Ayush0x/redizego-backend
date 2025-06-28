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

//    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point pickupLocation;

//    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point destinationLocation;

//    @CreationTimestamp
    private LocalDateTime requestTime;

//    @ManyToOne(fetch = FetchType.LAZY)
    private RiderDto rider;

//    @Enumerated(EnumType.STRING)
    private PaymentMethods paymentMethod;

//    @Enumerated(EnumType.STRING)
    private RideRequestStatus rideRequestStatus;

}
