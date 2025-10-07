package com.example.shop.listeners;

import com.example.shop.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventsListener {

    private final UserRepository userRepository;
    private static final int MAX_ATTEMPTS = 5;

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = String.valueOf(event.getAuthentication().getPrincipal());
        userRepository.findByUsername(username).ifPresent(u -> {
            int attempts = u.getFailedAttempt() + 1;
            u.setFailedAttempt(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                u.setAccountNonLocked(false);
                u.setLockTime(Instant.now());
            }
            userRepository.save(u);
        });
        log.warn("Auth failure user={} reason={}", username, event.getException().getMessage());
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setFailedAttempt(0);
            userRepository.save(u);
        });
        log.info("Auth success user={}", username);
    }
}
