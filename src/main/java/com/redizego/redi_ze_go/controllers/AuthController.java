package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.SignupDto;
import com.redizego.redi_ze_go.dtos.UserDto;
import com.redizego.redi_ze_go.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    UserDto signup(@RequestBody SignupDto signupDto){
        return authService.signup(signupDto);
    }
}
