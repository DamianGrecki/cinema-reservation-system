package org.example.services;

import static org.example.constants.Messages.INCORRECT_CREDENTIALS_MSG;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(INCORRECT_CREDENTIALS_MSG));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), Collections.emptyList());
    }
}
