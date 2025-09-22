package com.example.shop.service;

import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.Order;
import com.example.shop.model.User;
import com.example.shop.repo.OrderRepository;
import com.example.shop.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Order> findByUser(Long userId) {
        ensureUserExists(userId);
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Order getByIdForUser(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order " + orderId + " for user " + userId + " not found"));
    }

    public Order create(Long userId, Order payload) {
        User user = ensureUserExists(userId);
        payload.setId(null);
        payload.setUser(user);
        return orderRepository.save(payload);
    }

    public Order update(Long userId, Long orderId, Order payload) {
        Order existing = getByIdForUser(userId, orderId);
        existing.setItems(payload.getItems());
        existing.setTotalAmount(payload.getTotalAmount());
        existing.setStatus(payload.getStatus());
        return existing;
    }

    public void delete(Long userId, Long orderId) {
        Order existing = getByIdForUser(userId, orderId);
        orderRepository.delete(existing);
    }

    private User ensureUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));
    }
}
