package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.strategies.NetworkStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkStrategyImpl implements NetworkStrategy {
    @Override
    public Boolean isNetworkWeak(Double downloadSpeed, Double uploadSpeed) {
        if(downloadSpeed<256||uploadSpeed<512){
            return false;
        }
        else {
            return  true;
        }
    }
}
