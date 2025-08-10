package com.redizego.redi_ze_go.controllers;

import com.redizego.redi_ze_go.dtos.*;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.exceptions.RuntimeConflictException;
import com.redizego.redi_ze_go.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private SignupDto signupDto;
    private UserDto userDto;

    private LoginRequestDto loginRequestDto;
    private LoginResponseDto loginResponseDto;

    @BeforeEach
    void setUp() {
        signupDto=new SignupDto();
        userDto=new UserDto();
        loginRequestDto=new LoginRequestDto();
        loginResponseDto=new LoginResponseDto();

        signupDto.setEmail("test@example.com");
        signupDto.setPassword("password");
        signupDto.setName("test");

        userDto.setEmail("test@example.com");
        userDto.setName("test");
        userDto.setId(1L);

        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password");

        loginResponseDto.setAccessToken("accessToken");
    }

    @Test
    void testSignup_success() {
        when(authService.signup(signupDto)).thenReturn(userDto);

        ResponseEntity<UserDto> response=authController.signup(signupDto);

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(userDto,response.getBody());
    }

    @Test
    void testSignup_failure() {
        when(authService.signup(signupDto)).thenThrow(new RuntimeConflictException("User already exists with email "+signupDto.getEmail()));

        assertThrows(RuntimeConflictException.class,()->{
            authController.signup(signupDto);
        });
    }

    @Test
    void testOnboardDriver_success() {
        DriverDto driver=new DriverDto();

        when(authService.onboardDriver(1L,"vehicle123")).thenReturn(driver);

        ResponseEntity<DriverDto> response=authController.onboardDriver(1L,new OnboardDriverDto("vehicle123"));

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(driver,response.getBody());
    }

    @Test
    void testOnboardDriver_failure() {
        OnboardDriverDto onboardDriverDto=new OnboardDriverDto("vehicle123");

        when(authService.onboardDriver(1L,"vehicle123")).thenThrow(new RuntimeConflictException("User with id 1 is already a driver"));

        assertThrows(RuntimeConflictException.class,()->{
            authController.onboardDriver(1L,onboardDriverDto);
        });
    }

    @Test
    void testLogin_success() {
        String[] tokens={"accessToken","refreshToken"};

        HttpServletResponse response=new MockHttpServletResponse();

        when(authService.login(loginRequestDto.getEmail(),loginRequestDto.getPassword())).thenReturn(tokens);

        ResponseEntity<LoginResponseDto> responseEntity=authController.login(loginRequestDto,new MockHttpServletRequest(),response);

        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        assertEquals(loginResponseDto,responseEntity.getBody());
    }

    @Test
    void testLogin_failure() {
        when(authService.login(loginRequestDto.getEmail(),loginRequestDto.getPassword())).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class,()->{
            authController.login(loginRequestDto,new MockHttpServletRequest(),new MockHttpServletResponse());
        });
    }

    @Test
    void testRefreshToken_success() {
        HttpServletRequest request=new MockHttpServletRequest();
        Cookie cookie=new Cookie("refreshToken","valid.refresh.token");
        ((MockHttpServletRequest) request).setCookies(cookie);

        when(authService.refreshToken("valid.refresh.token")).thenReturn("accessToken");

        ResponseEntity<LoginResponseDto> response=authController.refreshToken(request);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(loginResponseDto,response.getBody());
    }

    @Test
    void testRefreshToken_failure() {
        MockHttpServletRequest request=new MockHttpServletRequest();

        assertThrows(AuthorizationServiceException.class,()->{
            authController.refreshToken(request);
        });
    }
}
