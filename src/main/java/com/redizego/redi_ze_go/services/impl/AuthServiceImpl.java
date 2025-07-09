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
import com.redizego.redi_ze_go.services.AuthService;
import com.redizego.redi_ze_go.services.DriverService;
import com.redizego.redi_ze_go.services.RiderService;
import com.redizego.redi_ze_go.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final RiderService riderService;
    private final WalletService walletService;
    private final DriverService driverService;

    @Override
    public String login(String email, String password) {
        return "";
    }

    @Override
    @Transactional
    public UserDto signup(SignupDto signupDto) {
        User user1=userRepository.findByEmail(signupDto.getEmail()).orElse(null);
        if(user1!=null)
            throw  new RuntimeConflictException("User already exists with email "+signupDto.getEmail());

        User user=modelMapper.map(signupDto,User.class);
        user.setRole(Set.of(Roles.RIDER));
        User savedUser=userRepository.save(user);

        Rider rider=riderService.createNewRider(savedUser);

        walletService.createNewWallet(savedUser);

        return modelMapper.map(savedUser,UserDto.class);
    }

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

    //TODO: implement driver onboarding or rather make the onboarding process such that until admin confirms driver is genuine, driver cannot be onboarded.
}
