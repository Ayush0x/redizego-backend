package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.strategies.impl.DriverMatchingHighestRatedDriverStrategy;
import com.redizego.redi_ze_go.strategies.impl.DriverMatchingNearestDriverStrategy;
import com.redizego.redi_ze_go.strategies.impl.RideFareDefaultFareCalculationStrategy;
import com.redizego.redi_ze_go.strategies.impl.RideFareSurgePricingFareCalculationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class RideStrategyManager {

    private final DriverMatchingHighestRatedDriverStrategy driverMatchingHighestRatedDriverStrategy;
    private final DriverMatchingNearestDriverStrategy driverMatchingNearestDriverStrategy;
    private final RideFareDefaultFareCalculationStrategy rideFareDefaultFareCalculationStrategy;
    private final RideFareSurgePricingFareCalculationStrategy rideFareSurgePricingFareCalculationStrategy;

    public DriverMatchingStrategy driverMatchingStrategy(double riderRating)
    {
        if(riderRating>=4.8)
        {
            return driverMatchingHighestRatedDriverStrategy;
        }
        else {
            return driverMatchingNearestDriverStrategy;
        }
    }

    public RideFareCalculationStrategy rideFareCalculationStrategy(){
        //6pm to 10pm
        LocalTime surgeStartTime=LocalTime.of(18,0);
        LocalTime surgeEndTime=LocalTime.of(22,0);
        LocalTime currentTime=LocalTime.now();
        boolean isSurgeTime=false;
        if(currentTime.isAfter(surgeStartTime)&&currentTime.isBefore(surgeEndTime)){
            isSurgeTime=true;
        }
        if(isSurgeTime){
            return rideFareSurgePricingFareCalculationStrategy;
        }
        else {
            return rideFareDefaultFareCalculationStrategy;
        }
    }
}
