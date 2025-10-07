package com.example.shop.config;

import com.example.shop.model.Role;
import com.example.shop.model.User;
import com.example.shop.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;


@Configuration
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class TestUserInitializer {

    @Bean
    CommandLineRunner initTestUser(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            String username = "test_user";
            String name = "alice";
            String email = "test_email@mail.ru";
            if (users.findByUsername(username).isEmpty()) {
                User u = new User();
                u.setUsername(username);
                u.setName(name);
                u.setEmail(email);
                u.setPassword(encoder.encode("correct_pass"));
                u.setAccountNonLocked(true);
                u.setFailedAttempt(0);
                u.setRoles(Set.of(Role.USER));
                users.save(u);
            }
        };
    }
}