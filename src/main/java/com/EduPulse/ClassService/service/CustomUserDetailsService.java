package com.EduPulse.ClassService.service;

import com.EduPulse.ClassService.model.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        UserResponse userResponse;
        try {
            userResponse = userServiceClient.getUserByUsername(usernameOrEmail);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found: " + usernameOrEmail, e);
        }

        if (userResponse == null) {
            throw new UsernameNotFoundException("User not found: " + usernameOrEmail);
        }

        String role = userResponse.getRole();

        // DO NOT add "ROLE_" again if it's already there!
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        return new User(
                userResponse.getUsername(),
                "",  // no password needed
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}