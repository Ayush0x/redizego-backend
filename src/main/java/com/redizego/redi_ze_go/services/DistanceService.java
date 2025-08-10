package com.redizego.redi_ze_go.services;

import org.locationtech.jts.geom.Point;

    public interface DistanceService {
        double calculateDistance(Point src,Point dest);
    }
