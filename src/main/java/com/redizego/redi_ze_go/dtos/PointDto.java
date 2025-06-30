package com.redizego.redi_ze_go.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class PointDto {

    private double[] coordinates;

    private String type="Point";

    public PointDto(double[] coordinates) {
        this.coordinates = coordinates;
    }

}
