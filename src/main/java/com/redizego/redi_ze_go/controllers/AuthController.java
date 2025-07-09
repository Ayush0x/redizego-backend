package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.OnboardDriverDto;
import com.redizego.redi_ze_go.dtos.SignupDto;
import com.redizego.redi_ze_go.dtos.UserDto;
import com.redizego.redi_ze_go.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupDto signupDto){
        return new ResponseEntity<>(authService.signup( signupDto ), HttpStatus.CREATED);
    }

    @PostMapping("/onboard-driver/{user-id}")
    public ResponseEntity<DriverDto> onboardDriver(@PathVariable("user-id") Long userId, @RequestBody OnboardDriverDto onboardDriverDto){
        return new ResponseEntity<>(authService.
                onboardDriver(userId,onboardDriverDto.getVehicleId()),
                HttpStatus.CREATED);
    }

}
