package com.example.shop.services.impl;

import com.example.shop.repositories.UserRepository;
import com.example.shop.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID uuid;
        try {
            uuid = UUID.fromString(username);
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("User not found");
        }

        return userRepository.findByUuid(uuid)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
