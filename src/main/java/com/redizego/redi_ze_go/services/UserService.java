package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username "+username));
    }

    public User getUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with id "+userId));
    }

}
