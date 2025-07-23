package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.SignupDto;
import com.redizego.redi_ze_go.dtos.UserDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.entities.enums.Roles;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.exceptions.RuntimeConflictException;
import com.redizego.redi_ze_go.repositories.UserRepository;
import com.redizego.redi_ze_go.security.JWTService;
import com.redizego.redi_ze_go.services.impl.AuthServiceImpl;
import com.redizego.redi_ze_go.services.impl.DriverServiceImpl;
import com.redizego.redi_ze_go.services.impl.RiderServiceImpl;
import com.redizego.redi_ze_go.services.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RiderServiceImpl riderService;

    @Mock
    private WalletServiceImpl walletService;

    @Mock
    private DriverServiceImpl driverService;

    @Spy
    private ModelMapper modelMapper;

    @Spy
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private UserDto userDto;
    private SignupDto signupDto;

    @BeforeEach
    void setUp(){
        user=new User();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@example.com");
        user.setPassword("Encodedpassword");
        user.setRole(new java.util.HashSet<>(Set.of(Roles.RIDER)));
    }

    @Test
    void testLogin_success(){
        String email="test@example.com";
        String rawPassword="password";
        String accessToken="accessToken";
        String refreshToken="refreshToken";

        Authentication auth=mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(any())).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(any())).thenReturn(refreshToken);

        String[] result=authService.login(email,rawPassword);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo(accessToken);
        assertThat(result[1]).isEqualTo(refreshToken);

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void testSignup_failure(){
        signupDto=new SignupDto();
        signupDto.setEmail("test@example.com");
        signupDto.setPassword("password");

        when(userRepository.findByEmail(signupDto.getEmail())).thenReturn(Optional.of(user));

        RuntimeConflictException exception=assertThrows(RuntimeConflictException.class,()->{
            authService.signup(signupDto);});

        assertEquals("User already exists with email "+signupDto.getEmail(),exception.getMessage());
    }

    @Test
    void testSignup_success(){
        signupDto=new SignupDto();
        signupDto.setEmail("test@example.com");
        signupDto.setPassword("password");
        signupDto.setName("test");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(modelMapper.map(signupDto,User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userDto=authService.signup(signupDto);

        ArgumentCaptor<User> captor=ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser=captor.getValue();

        // Verify the saved user has the correct properties
        assertNotNull(savedUser);
        assertEquals(signupDto.getEmail(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertTrue(savedUser.getRole().contains(Roles.RIDER));

        // Verify interactions
        verify(userRepository, times(1)).save(any(User.class));
        verify(riderService, times(1)).createNewRider(any(User.class));
        verify(walletService, times(1)).createNewWallet(any(User.class));
        assertNotNull(userDto, "UserDto should not be null");
    }

    @Test
    void testOnboardDriver_whenUserExistsAndNotDriver_shouldSucceed() {
        // Arrange
        String vehicleId = "vehicle123";
        user.setRole(new java.util.HashSet<>(Set.of(Roles.RIDER)));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Driver savedDriver = new Driver();
        when(driverService.createNewDriver(any(Driver.class))).thenReturn(savedDriver);

        DriverDto expectedDriverDto = new DriverDto();
        when(modelMapper.map(savedDriver, DriverDto.class)).thenReturn(expectedDriverDto);

        // Act
        DriverDto result = authService.onboardDriver(user.getId(), vehicleId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDriverDto, result);

        // Verify user was updated with DRIVER role
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertTrue(updatedUser.getRole().contains(Roles.DRIVER));

        // Verify driver creation
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverService).createNewDriver(driverCaptor.capture());
        Driver createdDriver = driverCaptor.getValue();
        assertEquals(user, createdDriver.getUser());
        assertEquals(vehicleId, createdDriver.getVehicleId());
    }

    @Test
    void testOnboardDriver_whenUserIsAlreadyDriver_shouldThrowException() {
        // Arrange
        user.setRole(new java.util.HashSet<>(Set.of(Roles.DRIVER)));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeConflictException exception = assertThrows(RuntimeConflictException.class, () -> {
            authService.onboardDriver(user.getId(), "vehicle123");
        });

        assertEquals("User with id " + user.getId() + " is already a driver", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(driverService, never()).createNewDriver(any(Driver.class));
    }

    @Test
    void testOnboardDriver_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authService.onboardDriver(nonExistentUserId, "vehicle123");
        });

        assertEquals("User not found with id " + nonExistentUserId, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(driverService, never()).createNewDriver(any(Driver.class));
    }

    @Test
    void testRefreshToken_success() {
        // Arrange
        String refreshToken = "valid.refresh.token";
        String newAccessToken = "new.access.token";
        Long userId = 1L;

        when(jwtService.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);

        // Act
        String result = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(newAccessToken, result);
        verify(jwtService).getUserIdFromToken(refreshToken);
        verify(userRepository).findById(userId);
        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void testRefreshToken_whenUserNotFound_shouldThrowException() {
        // Arrange
        String refreshToken = "valid.refresh.token";
        Long nonExistentUserId = 999L;

        when(jwtService.getUserIdFromToken(refreshToken)).thenReturn(nonExistentUserId);
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authService.refreshToken(refreshToken);
        });

        assertEquals("User not found with id " + nonExistentUserId, exception.getMessage());
        verify(jwtService).getUserIdFromToken(refreshToken);
        verify(userRepository).findById(nonExistentUserId);
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void testLogin_authenticationFails_shouldPropagateException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        // Act & Assert
        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> {
            authService.login(email, password);
        });

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtService);
    }
}
