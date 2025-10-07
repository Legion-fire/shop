package com.example.shop.service;

import com.example.shop.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final UserRepository userRepository;

    private static final int MAX_ATTEMPTS = 5;

    @Transactional
    public void loginSucceeded(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setFailedAttempt(0);
            u.setAccountNonLocked(true);
            u.setLockTime(null);
        });
    }

    @Transactional
    public void loginFailed(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            int attempts = u.getFailedAttempt() + 1;
            u.setFailedAttempt(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                u.setAccountNonLocked(false);
                u.setLockTime(Instant.from(LocalDateTime.now()));
            }
        });
    }

    @Transactional
    public void unlock(String username) {   userRepository.findByUsername(username).ifPresent(u -> {
        u.setAccountNonLocked(true);
        u.setFailedAttempt(0);
        u.setLockTime(null);
    });
    }
}