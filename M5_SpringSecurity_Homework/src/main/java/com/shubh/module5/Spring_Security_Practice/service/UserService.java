package com.shubh.module5.Spring_Security_Practice.service;


import com.shubh.module5.Spring_Security_Practice.dto.SignUpDTO;
import com.shubh.module5.Spring_Security_Practice.dto.UserResponseDTO;
import com.shubh.module5.Spring_Security_Practice.entity.User;
import com.shubh.module5.Spring_Security_Practice.exception.ResourceNotFoundException;
import com.shubh.module5.Spring_Security_Practice.repository.UserRepository;
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
        // Used in JWT Creation
        return userRepository.findByEmail(username)
                // Spring Security intentionally avoids revealing whether the username
                // exists or the password is incorrect (prevents username enumeration).
                //
                // During authentication, provider-specific exceptions (e.g.,
                // UsernameNotFoundException) are often translated into a generic
                // AuthenticationException such as BadCredentialsException.
                //
                // Therefore, we throw BadCredentialsException with a generic message
                // instead of exposing user existence details.
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    public UserResponseDTO signUp(SignUpDTO signUpRequest) {
        String email = signUpRequest.getEmail();
        Boolean emailExists = userRepository.existsByEmail(email);
        if (emailExists)
            throw new BadCredentialsException("Email " + email + " already exists");

        String password = signUpRequest.getPasswordHash();

        User userToBeSaved = modelMapper.map(signUpRequest, User.class);
        userToBeSaved.setPasswordHash(passwordEncoder.encode(password));

        return modelMapper.map(userRepository.save(userToBeSaved), UserResponseDTO.class);
    }

    public User getUserByID(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found"));
    }
}
