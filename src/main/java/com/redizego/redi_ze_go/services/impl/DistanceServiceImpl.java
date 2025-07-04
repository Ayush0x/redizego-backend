package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.services.DistanceService;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class DistanceServiceImpl implements DistanceService {

    private static final String OSRM_API="http://router.project-osrm.org/route/v1/driving/";

    @Override
    public double calculateDistance(Point src, Point dest) {

        try {
            String uri = src.getX() + "," + src.getY() + ";" + dest.getX() + "," + dest.getY();

            OSRMResponseDto response = RestClient.builder()
                    .baseUrl(OSRM_API)
                    .build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(OSRMResponseDto.class);

            Double distance = response.getRoutes().get(0).getDistance();
            return distance / 1000.0; // Convert meters to kilometers

        } catch (Exception e) {
            throw new RuntimeException("Error calculating distance: " + e.getMessage(), e);
        }
    }
}

@Data
class OSRMResponseDto{
    List<OSRMRoute> routes;
}

@Data
class OSRMRoute{
    private Double distance;
}
