package com.shubh.module5.Spring_Security_Demo.service;

import com.shubh.module5.Spring_Security_Demo.dto.SignUpDTO;
import com.shubh.module5.Spring_Security_Demo.dto.UserDTO;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.exception.ResourceNotFoundException;
import com.shubh.module5.Spring_Security_Demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    UserRepository userRepository;
    ModelMapper modelMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                // Authentication Exception's Child
                .orElseThrow(() -> new BadCredentialsException("User with email :" + username + " not found"));

    }

    public UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id :" + id + " not found"));
    }

    public UserDTO signUp(SignUpDTO signUpRequest) {
        Boolean isUserWithEmailPresent = userRepository.existsByEmail(signUpRequest.getEmail());
        if (isUserWithEmailPresent)
            throw new BadCredentialsException("User with email " + signUpRequest.getEmail() + " already exists");

        UserEntity userToBeSaved = modelMapper.map(signUpRequest, UserEntity.class);
        // We manually hash the raw password using PasswordEncoder.encode()
        // before persisting it to the database.
        userToBeSaved.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        return modelMapper.map(userRepository.save(userToBeSaved), UserDTO.class);
    }


    /**
     * Login Logic moved to a separate service because of circular dependency between
     * Authentication Manager and UserDetails Service. See WebSecurityConfig File
     * ┌─────┐
     * |  userService defined in file [/Users/shubhgaur/Documents/SpringBOOT/M5_Spring_Security_Demo/target/classes/com/shubh/module5/Spring_Security_Demo/service/UserService.class]
     * ↑     ↓
     * |  authenticationManager defined in class path resource [com/shubh/module5/Spring_Security_Demo/config/WebSecurityConfig.class]
     * └─────┘
     */
}
