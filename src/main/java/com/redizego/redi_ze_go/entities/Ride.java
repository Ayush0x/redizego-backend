package com.redizego.redi_ze_go.entities;

import com.redizego.redi_ze_go.entities.enums.PaymentMethods;
import com.redizego.redi_ze_go.entities.enums.RideStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_ride_rider",columnList = "rider_id"),
        @Index(name = "idx_ride_driver",columnList = "driver_id")
})
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point pickupLocation;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point destinationLocation;

    @CreationTimestamp
    private LocalDateTime requestTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    private Rider rider;

    @Enumerated(EnumType.STRING)
    private RideStatus rideStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethods paymentMethod;

    private Double fare;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String otp;
}

