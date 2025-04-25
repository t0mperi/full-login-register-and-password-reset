package com.example.loginregisterreset.service;

import com.example.loginregisterreset.dto.UserRegistrationDto;
import com.example.loginregisterreset.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User save(UserRegistrationDto registrationDto);
    User findByEmail(String email);
}
