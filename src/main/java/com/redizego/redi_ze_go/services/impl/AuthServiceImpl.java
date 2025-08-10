package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.DriverDto;
import com.redizego.redi_ze_go.dtos.SignupDto;
import com.redizego.redi_ze_go.dtos.UserDto;
import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.entities.enums.Roles;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.exceptions.RuntimeConflictException;
import com.redizego.redi_ze_go.repositories.UserRepository;
import com.redizego.redi_ze_go.security.JWTService;
import com.redizego.redi_ze_go.services.AuthService;
import com.redizego.redi_ze_go.services.DriverService;
import com.redizego.redi_ze_go.services.RiderService;
import com.redizego.redi_ze_go.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service implementation for authentication and user management operations.
 * 
 * This service handles all authentication-related functionality including user
 * registration, login, token management, and role-based onboarding. It integrates
 * with Spring Security for authentication and JWT for token management.
 * 
 * Key Features:
 * - User registration with automatic rider profile creation
 * - Secure password encoding and authentication
 * - JWT token generation (access + refresh tokens)
 * - Driver onboarding with role management
 * - Token refresh mechanism
 * - Automatic wallet creation for new users
 * - Comprehensive validation and conflict detection
 * 
 * Security Features:
 * - Password encryption using configurable encoder
 * - JWT-based stateless authentication
 * - Role-based access control (RIDER/DRIVER)
 * - Duplicate email prevention
 * - Secure token refresh mechanism
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** ModelMapper for entity-DTO conversions */
    private final ModelMapper modelMapper;
    
    /** Repository for user data access and persistence */
    private final UserRepository userRepository;
    
    /** Service for rider profile management */
    private final RiderService riderService;
    
    /** Service for wallet creation and management */
    private final WalletService walletService;
    
    /** Service for driver profile management */
    private final DriverService driverService;
    
    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;
    
    /** Spring Security authentication manager */
    private final AuthenticationManager authenticationManager;
    
    /** JWT service for token generation and validation */
    private final JWTService jwtService;

    /**
     * Authenticates a user and generates JWT tokens.
     * 
     * This method performs user authentication using Spring Security's
     * authentication manager and returns both access and refresh tokens
     * for subsequent API calls.
     * 
     * Authentication Process:
     * 1. Validates credentials using AuthenticationManager
     * 2. Extracts authenticated user from security context
     * 3. Generates access token for API access
     * 4. Generates refresh token for token renewal
     * 
     * @param email The user's email address for authentication
     * @param password The user's password (will be verified against encoded password)
     * @return String[] Array containing [accessToken, refreshToken]
//     * @throws AuthenticationException if credentials are invalid
     */
    @Override
    public String[] login(String email, String password) {
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));

        User user=(User) authentication.getPrincipal();

        String accessToken=jwtService.generateAccessToken(user);
        String refreshToken=jwtService.generateRefreshToken(user);


        return new String[]{accessToken,refreshToken};
    }

    /**
     * Registers a new user with automatic profile and wallet creation.
     * 
     * This method handles complete user onboarding including:
     * 1. Email uniqueness validation
     * 2. Secure password encoding
     * 3. Default RIDER role assignment
     * 4. Automatic rider profile creation
     * 5. Digital wallet initialization
     * 
     * The registration process is transactional to ensure data consistency
     * across user, rider, and wallet creation operations.
     * 
     * @param signupDto User registration data (name, email, password, etc.)
     * @return UserDto The created user information (excluding password)
     * @throws RuntimeConflictException if email already exists in system
     */
    @Override
    @Transactional
    public UserDto signup(SignupDto signupDto) {
        User user1=userRepository.findByEmail(signupDto.getEmail()).orElse(null);
        if(user1!=null)
            throw  new RuntimeConflictException("User already exists with email "+signupDto.getEmail());

        User user=modelMapper.map(signupDto,User.class);
        user.setRole(Set.of(Roles.RIDER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser=userRepository.save(user);

        riderService.createNewRider(savedUser);

        walletService.createNewWallet(savedUser);

        return modelMapper.map(savedUser,UserDto.class);
    }

    /**
     * Onboards an existing rider as a driver with vehicle registration.
     * 
     * This method allows existing users to become drivers by:
     * 1. Validating user existence
     * 2. Checking for duplicate driver registration
     * 3. Creating driver profile with vehicle information
     * 4. Adding DRIVER role to existing user roles
     * 5. Setting initial availability and rating
     * 
     * Users can have both RIDER and DRIVER roles simultaneously,
     * enabling them to both request and provide rides.
     * 
     * @param userId The ID of the existing user to onboard as driver
     * @param vehicleId Vehicle identification/registration number
     * @return DriverDto The created driver profile information
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws RuntimeConflictException if user is already a driver
     */
    @Override
    public DriverDto onboardDriver(Long userId,String vehicleId) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with id "+userId));

        if(user.getRole().contains(Roles.DRIVER)){
            throw new RuntimeConflictException("User with id "+userId+" is already a driver");
        }

        Driver createdDriver= Driver
                .builder()
                .user(user)
                .rating(0.0)
                .vehicleId(vehicleId)
                .isAvailable(true)
                .build();

        user.getRole().add(Roles.DRIVER);
        userRepository.save(user);
        Driver savedDriver=driverService.createNewDriver(createdDriver);

        return modelMapper.map(savedDriver,DriverDto.class);

    }

    /**
     * Refreshes an access token using a valid refresh token.
     * 
     * This method enables token renewal without requiring users to
     * re-authenticate. The refresh token is validated and used to
     * generate a new access token with updated expiration.
     * 
     * Refresh Process:
     * 1. Extracts user ID from refresh token
     * 2. Validates refresh token authenticity
     * 3. Retrieves current user information
     * 4. Generates new access token with fresh expiration
     * 
     * @param refreshToken Valid refresh token for token renewal
     * @return String New access token for continued API access
     * @throws ResourceNotFoundException if user associated with token doesn't exist
//     * @throws JwtException if refresh token is invalid or expired
     */
    @Override
    public String refreshToken(String refreshToken) {
        Long userId=jwtService.getUserIdFromToken(refreshToken);
        User user=userRepository.findById(userId)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with id "+userId));
        String accessToken=jwtService.generateAccessToken(user);
        return accessToken;
    }

}
