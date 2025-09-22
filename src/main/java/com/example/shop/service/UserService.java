package com.example.shop.service;

import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.User;
import com.example.shop.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> findAll() { return userRepository.findAll(); }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    public User create(User user) {
        // простая защита от дублей email
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    public User update(Long id, User payload) {
        User existing = getById(id);
        existing.setName(payload.getName());
        existing.setEmail(payload.getEmail());
        existing.setAddress(payload.getAddress());
        return existing;
    }
    public void delete(Long id) {
        User existing = getById(id);
        userRepository.delete(existing);
    }
}
