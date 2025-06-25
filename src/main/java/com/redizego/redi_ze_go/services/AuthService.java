package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.SignupDto;
import com.redizego.redi_ze_go.dtos.UserDto;

public interface AuthService {
    String login(String email, String password);
    UserDto signup(SignupDto signupDto);

    DriverDto onboardDriver(Long usserId);


}
