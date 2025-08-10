package com.redizego.redi_ze_go.utils;

import com.redizego.redi_ze_go.dtos.PointDto;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryUtil {

    public static Point createPoint(PointDto pointDto) {
        if (pointDto == null || pointDto.getCoordinates() == null) {
            throw new IllegalArgumentException("PointDto and its coordinates must not be null");
        }
        if (pointDto.getCoordinates().length < 2) {
            throw new IllegalArgumentException("Coordinates array must have at least 2 elements [longitude, latitude]");
        }
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate coordinate = new Coordinate(pointDto.getCoordinates()[0], pointDto.getCoordinates()[1]);
        return geometryFactory.createPoint(coordinate);
    }
}
