package com.redizego.redi_ze_go.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverDto {

    private UserDto user;

    private Double rating;

    private String vehicleId;
}
