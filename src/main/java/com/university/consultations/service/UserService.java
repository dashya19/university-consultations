package com.university.consultations.service;

import com.university.consultations.entity.User;
import com.university.consultations.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);

        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> {
                    logger.error("User not found with login: {}", username);
                    return new UsernameNotFoundException("User not found with login: " + username);
                });

        logger.info("Found user: {}, password hash: {}, roles: {}",
                user.getLogin(), user.getPasswordHash(), user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
    }
}