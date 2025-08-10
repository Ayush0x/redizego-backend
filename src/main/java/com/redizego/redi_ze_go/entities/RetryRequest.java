package com.redizego.redi_ze_go.entities;

import com.redizego.redi_ze_go.entities.enums.RetryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * Entity representing a ride request that failed during initial processing and needs to be retried.
 * 
 * This entity stores failed ride requests in a queue for later processing by the
 * RetryScheduledService. It maintains the original request information along with
 * retry metadata to ensure proper handling and prevent infinite retry loops.
 * 
 * Key Features:
 * - Stores pickup and destination locations as PostGIS Point geometries
 * - Tracks retry attempts and current status
 * - Associates with the original rider who made the request
 * - Records creation timestamp for monitoring and cleanup
 * 
 * The retry mechanism follows these states:
 * PENDING -> SUCCESS (if retry succeeds)
 * PENDING -> FAILED (if maximum retry attempts exceeded)
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see RetryStatus
 * @see com.redizego.redi_ze_go.services.impl.RetryScheduledServiceImpl
 */
@Entity
@Table(name = "retry_requests", indexes = {
    @Index(name = "idx_retry_status", columnList = "retry_status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryRequest {

    /**
     * Unique identifier for the retry request.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Pickup location as a PostGIS Point geometry.
     * 
     * Uses SRID 4326 (WGS 84) coordinate system for GPS coordinates.
     * Format: POINT(longitude latitude)
     * 
     * @implNote Stored as geometry(Point, 4326) in PostgreSQL with PostGIS extension
     */
    @Column(name = "pickup_location", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point pickupLocation;

    /**
     * Destination location as a PostGIS Point geometry.
     * 
     * Uses SRID 4326 (WGS 84) coordinate system for GPS coordinates.
     * Format: POINT(longitude latitude)
     * 
     * @implNote Stored as geometry(Point, 4326) in PostgreSQL with PostGIS extension
     */
    @Column(name = "destination_location", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point destinationLocation;

    /**
     * Timestamp when the retry request was created.
     * 
     * Used for monitoring, cleanup, and determining request age.
     * Should be set when the retry request is first created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Number of retry attempts made for this request.
     * 
     * Starts at 0 and increments with each failed retry attempt.
     * When this reaches the maximum threshold (typically 3),
     * the status should be changed to FAILED.
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    /**
     * The rider who originally made the ride request.
     * 
     * This association helps in processing the retry request
     * and maintaining the connection to the original requester.
     * 
     * @implNote Uses lazy loading to optimize query performance
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private Rider rider;

    /**
     * Current status of the retry request.
     * 
     * Possible values:
     * - PENDING: Waiting for retry processing
     * - SUCCESS: Successfully processed during retry
     * - FAILED: Failed after maximum retry attempts
     * 
     * @see RetryStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "retry_status", nullable = false)
    @Builder.Default
    private RetryStatus retryStatus = RetryStatus.PENDING;
    
    /**
     * Sets the creation timestamp to current time before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
