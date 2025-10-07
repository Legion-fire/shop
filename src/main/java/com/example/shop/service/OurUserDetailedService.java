package com.example.shop.service;



import com.example.shop.model.User;
import com.example.shop.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.example.shop.model.User.LOCK_DURATION;


@Service
@RequiredArgsConstructor
public class OurUserDetailedService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (u.unlockIfExpired(LOCK_DURATION)) {
            userRepository.save(u);
        }
        return u;
    }
}
