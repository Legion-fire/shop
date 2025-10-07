package com.example.shop.listeners;

import com.example.shop.model.User;
import com.example.shop.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthEventsListener {

    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Value("${security.auth.lock-duration:PT15M}")
    private Duration lockDuration;

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        String username = resolveUsername(auth);
        var ex = event.getException();

        log.warn("Auth failure user={} reason={}", username, ex.getClass().getSimpleName());

        if (username == null) return;

        userRepository.findByUsername(username).ifPresent(u -> {
            if (!u.isAccountNonLocked() && isLockExpired(u)) {
                unlock(u);
            }

            if (ex instanceof BadCredentialsException) {
                int attempts = u.getFailedAttempt() + 1;
                u.setFailedAttempt(attempts);

                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    lock(u);
                }
            } else if (ex instanceof LockedException) {
                if (u.isAccountNonLocked()) {
                    lock(u);
                } else if (isLockExpired(u)) {
                    unlock(u);
                }
            }

            userRepository.save(u);
        });
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("Auth success user={}", username);

        userRepository.findByUsername(username).ifPresent(u -> {
            u.setFailedAttempt(0);
            if (!u.isAccountNonLocked()) {
                u.setAccountNonLocked(true);
                u.setLockTime(null);
            }
            userRepository.save(u);
        });
    }

    private String resolveUsername(Authentication auth) {
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) return ud.getUsername();
        if (principal instanceof String s) return s;
        return auth.getName();
    }

    private boolean isLockExpired(User u) {
        Instant lockAt = u.getLockTime();
        if (lockAt == null) return true;
        return Instant.now().isAfter(lockAt.plus(lockDuration));
    }

    private void lock(User u) {
        u.setAccountNonLocked(false);
        u.setLockTime(Instant.now());
    }

    private void unlock(User u) {
        u.setAccountNonLocked(true);
        u.setLockTime(null);
        u.setFailedAttempt(0);
    }
}
