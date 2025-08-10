package com.redizego.redi_ze_go.configs;

import com.redizego.redi_ze_go.dtos.PointDto;
import com.redizego.redi_ze_go.utils.GeometryUtil;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for ModelMapper bean and custom type conversions.
 * 
 * This configuration provides a centralized ModelMapper instance with custom
 * converters for handling complex data type transformations, particularly
 * between geometric types and DTOs used in the ride-hailing system.
 * 
 * Key Features:
 * - Configured ModelMapper with sensible defaults
 * - Custom converters for PostGIS Point <-> PointDto transformations
 * - Null-safe conversion handling
 * - Error handling for invalid geometric data
 * 
 * Custom Converters:
 * - PointDto to PostGIS Point: For database storage
 * - PostGIS Point to PointDto: For API responses
 * 
 * Configuration Settings:
 * - Skip null values during mapping to avoid overwriting with nulls
 * - Ignore ambiguous mappings to prevent runtime errors
 * - Strict field access for better performance
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see PointDto
 * @see GeometryUtil
 * @see org.modelmapper.ModelMapper
 * 
 * @implNote ModelMapper is thread-safe and can be used as singleton
 */
@Slf4j
@Configuration
public class MapperConfig {

    /**
     * Creates and configures a ModelMapper bean with custom converters.
     * 
     * This method sets up ModelMapper with:
     * - Null value skipping to prevent null overwrites
     * - Ambiguity ignoring to handle complex object mappings
     * - Custom converters for geometric data types
     * - Error handling for conversion failures
     * 
     * Custom Type Mappings:
     * 1. PointDto -> PostGIS Point: Converts API coordinate data to database geometry
     * 2. PostGIS Point -> PointDto: Converts database geometry to API coordinate data
     * 
     * @return Configured ModelMapper instance ready for dependency injection
     * 
     * @implNote ModelMapper instance is singleton and thread-safe
     * @implNote Custom converters handle null values gracefully
     */
    @Bean
    public ModelMapper modelMapper() {
        log.debug("Initializing ModelMapper with custom configurations");
        
        ModelMapper modelMapper = new ModelMapper();
        
        try {
            // Configure ModelMapper global settings
            modelMapper.getConfiguration()
                    .setSkipNullEnabled(true)        // Don't overwrite with null values
                    .setAmbiguityIgnored(true)       // Handle ambiguous mappings gracefully
                    .setFieldMatchingEnabled(true)   // Enable field-level mapping
                    .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
            
            // Register custom converters
            registerPointConverters(modelMapper);
            
            log.info("ModelMapper successfully configured with custom converters");
            return modelMapper;
            
        } catch (Exception e) {
            log.error("Failed to configure ModelMapper: {}", e.getMessage(), e);
            throw new RuntimeException("ModelMapper configuration failed", e);
        }
    }
    
    /**
     * Registers custom converters for Point-related type mappings.
     * 
     * @param modelMapper the ModelMapper instance to configure
     */
    private void registerPointConverters(ModelMapper modelMapper) {
        log.debug("Registering custom Point converters");
        
        // PointDto to PostGIS Point converter
        modelMapper.typeMap(PointDto.class, Point.class)
                .setConverter(context -> {
                    PointDto pointDto = context.getSource();
                    
                    if (pointDto == null) {
                        log.debug("Converting null PointDto to null Point");
                        return null;
                    }
                    
                    try {
                        Point point = GeometryUtil.createPoint(pointDto);
                        log.debug("Successfully converted PointDto to Point: [{}, {}]", 
                                pointDto.getCoordinates()[0], pointDto.getCoordinates()[1]);
                        return point;
                        
                    } catch (IllegalArgumentException e) {
                        log.error("Failed to convert PointDto to Point: {}", e.getMessage());
                        throw new IllegalArgumentException("Invalid point data: " + e.getMessage(), e);
                    }
                });

        // PostGIS Point to PointDto converter  
        modelMapper.typeMap(Point.class, PointDto.class)
                .setConverter(context -> {
                    Point point = context.getSource();
                    
                    if (point == null) {
                        log.debug("Converting null Point to null PointDto");
                        return null;
                    }
                    
                    try {
                        // Extract coordinates from Point geometry
                        double[] coordinates = {
                                point.getX(),  // Longitude
                                point.getY()   // Latitude
                        };
                        
                        PointDto pointDto = new PointDto(coordinates);
                        log.debug("Successfully converted Point to PointDto: [{}, {}]", 
                                coordinates[0], coordinates[1]);
                        return pointDto;
                        
                    } catch (Exception e) {
                        log.error("Failed to convert Point to PointDto: {}", e.getMessage());
                        throw new RuntimeException("Point to PointDto conversion failed", e);
                    }
                });
        
        log.debug("Point converters successfully registered");
    }
}
