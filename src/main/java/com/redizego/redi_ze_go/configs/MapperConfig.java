package com.redizego.redi_ze_go.configs;

import com.redizego.redi_ze_go.dtos.PointDto;
import com.redizego.redi_ze_go.utils.GeometryUtil;
import org.locationtech.jts.geom.Point;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        // Configure to skip null values
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);
                
        // PointDto to Point converter
        modelMapper.typeMap(PointDto.class, Point.class)
                .setConverter(context -> {
                    PointDto pointDto = context.getSource();
                    if (pointDto == null) {
                        return null;
                    }
                    try {
                        return GeometryUtil.createPoint(pointDto);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid point data: " + e.getMessage(), e);
                    }
                });

        // Point to PointDto converter
        modelMapper.typeMap(Point.class, PointDto.class)
                .setConverter(context -> {
                    Point point = context.getSource();
                    if (point == null) {
                        return null;
                    }
                    double coordinates[] = {
                            point.getX(),
                            point.getY()
                    };
                    return new PointDto(coordinates);
                });

        return modelMapper;
    }
}
