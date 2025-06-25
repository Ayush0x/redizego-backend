package com.redizego.redi_ze_go.services;

import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

public interface DistanceService {
    double calculateDistance(Point src,Point dest);
}
