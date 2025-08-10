package com.redizego.redi_ze_go.utils;

import com.redizego.redi_ze_go.dtos.PointDto;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Utility class for geometric operations and conversions in the ride-hailing system.
 * 
 * This class provides static methods for converting between different geometric
 * representations, primarily focusing on coordinate transformations between
 * DTO objects and PostGIS Point geometries.
 * 
 * Key Features:
 * - Conversion from PointDto to PostGIS Point geometry
 * - SRID 4326 (WGS84) coordinate system support
 * - Input validation and error handling
 * - Thread-safe static methods
 * 
 * Coordinate System:
 * Uses SRID 4326 (World Geodetic System 1984) which is the standard
 * coordinate system for GPS and web mapping applications.
 * 
 * Coordinate Format:
 * - Index 0: Longitude (X coordinate, -180 to +180)
 * - Index 1: Latitude (Y coordinate, -90 to +90)
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see PointDto
 * @see com.redizego.redi_ze_go.configs.MapperConfig
 * @see org.locationtech.jts.geom.Point
 * 
 * @implNote All methods are static and thread-safe
 * @implNote Uses JTS (Java Topology Suite) for geometric operations
 */
@Slf4j
public final class GeometryUtil {
    
    /** SRID for WGS84 coordinate system used by GPS and web mapping */
    private static final int WGS84_SRID = 4326;
    
    /** Minimum coordinate array length required for 2D points */
    private static final int MIN_COORDINATE_LENGTH = 2;
    
    /** Longitude bounds: -180 to +180 degrees */
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    
    /** Latitude bounds: -90 to +90 degrees */
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private GeometryUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a PostGIS Point geometry from a PointDto object.
     * 
     * This method converts coordinate data from the DTO format used in the API
     * to the PostGIS Point geometry format used in the database. The resulting
     * Point uses SRID 4326 (WGS84) coordinate system.
     * 
     * Coordinate Validation:
     * - Longitude must be between -180 and +180 degrees
     * - Latitude must be between -90 and +90 degrees
     * - Coordinates array must have at least 2 elements
     * 
     * @param pointDto The PointDto containing coordinate data
     * @return PostGIS Point geometry with SRID 4326
     * @throws IllegalArgumentException if pointDto is null, coordinates are null,
     *                                  array is too short, or coordinates are out of valid range
     * 
     * @example
     * PointDto dto = new PointDto(new double[]{77.5946, 12.9716}); // Bangalore coordinates
     * Point point = GeometryUtil.createPoint(dto);
     * 
     * @implNote Coordinates are stored as [longitude, latitude] in the array
     * @implNote Uses high precision model for accurate geometric calculations
     */
    public static Point createPoint(PointDto pointDto) {
        // Validate input parameters
        if (pointDto == null) {
            log.error("Cannot create Point: PointDto is null");
            throw new IllegalArgumentException("PointDto must not be null");
        }
        
        if (pointDto.getCoordinates() == null) {
            log.error("Cannot create Point: coordinates array is null");
            throw new IllegalArgumentException("Coordinates array must not be null");
        }
        
        if (pointDto.getCoordinates().length < MIN_COORDINATE_LENGTH) {
            log.error("Cannot create Point: coordinates array length is {}, minimum required is {}", 
                    pointDto.getCoordinates().length, MIN_COORDINATE_LENGTH);
            throw new IllegalArgumentException(
                    "Coordinates array must have at least 2 elements [longitude, latitude]");
        }
        
        double longitude = pointDto.getCoordinates()[0];
        double latitude = pointDto.getCoordinates()[1];
        
        // Validate coordinate ranges
        validateLongitude(longitude);
        validateLatitude(latitude);
        
        try {
            // Create geometry factory with WGS84 SRID and high precision
            GeometryFactory geometryFactory = new GeometryFactory(
                    new PrecisionModel(PrecisionModel.FLOATING), WGS84_SRID);
            
            // Create coordinate and point geometry
            Coordinate coordinate = new Coordinate(longitude, latitude);
            Point point = geometryFactory.createPoint(coordinate);
            
            log.debug("Created Point geometry: SRID={}, Longitude={}, Latitude={}", 
                    WGS84_SRID, longitude, latitude);
            
            return point;
            
        } catch (Exception e) {
            log.error("Failed to create Point geometry from coordinates [{}, {}]: {}", 
                    longitude, latitude, e.getMessage(), e);
            throw new IllegalArgumentException(
                    "Failed to create Point geometry: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates that longitude is within the valid range.
     * 
     * @param longitude the longitude value to validate
     * @throws IllegalArgumentException if longitude is out of range
     */
    private static void validateLongitude(double longitude) {
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            log.error("Invalid longitude: {}. Must be between {} and {}", 
                    longitude, MIN_LONGITUDE, MAX_LONGITUDE);
            throw new IllegalArgumentException(
                    String.format("Longitude must be between %.1f and %.1f degrees, got: %.6f", 
                            MIN_LONGITUDE, MAX_LONGITUDE, longitude));
        }
    }
    
    /**
     * Validates that latitude is within the valid range.
     * 
     * @param latitude the latitude value to validate
     * @throws IllegalArgumentException if latitude is out of range
     */
    private static void validateLatitude(double latitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            log.error("Invalid latitude: {}. Must be between {} and {}", 
                    latitude, MIN_LATITUDE, MAX_LATITUDE);
            throw new IllegalArgumentException(
                    String.format("Latitude must be between %.1f and %.1f degrees, got: %.6f", 
                            MIN_LATITUDE, MAX_LATITUDE, latitude));
        }
    }
    
    /**
     * Extracts coordinate array from a PostGIS Point geometry.
     * 
     * This is a convenience method for converting Point geometries back
     * to coordinate arrays compatible with PointDto objects.
     * 
     * @param point the PostGIS Point geometry
     * @return coordinate array [longitude, latitude]
     * @throws IllegalArgumentException if point is null
     */
    public static double[] extractCoordinates(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }
        
        return new double[] { point.getX(), point.getY() };
    }
}
