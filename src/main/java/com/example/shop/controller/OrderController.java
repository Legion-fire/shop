package com.example.shop.controller;

import com.example.shop.model.Order;
import com.example.shop.service.OrderService;
import com.example.shop.view.*;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Список заказов пользователя (краткое представление)
    @GetMapping
    @JsonView(OrderSummary.class)
    public List<Order> list(@PathVariable Long userId) {
        return orderService.findByUser(userId);
    }

    // Детали заказа (расширенное представление)
    @GetMapping("/{orderId}")
    @JsonView(OrderSummary.class)
    public Order get(@PathVariable Long userId, @PathVariable Long orderId) {
        return orderService.getByIdForUser(userId, orderId);
    }

    // Создание заказа
    @PostMapping
    public ResponseEntity<Order> create(@PathVariable Long userId,
                                        @Valid @RequestBody Order payload,
                                        UriComponentsBuilder uriBuilder) {
        Order created = orderService.create(userId, payload);
        URI location = uriBuilder.path("/api/users/{userId}/orders/{orderId}")
                .buildAndExpand(userId, created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Обновление заказа
    @PutMapping("/{orderId}")
    public Order update(@PathVariable Long userId,
                        @PathVariable Long orderId,
                        @Valid @RequestBody Order payload) {
        return orderService.update(userId, orderId, payload);
    }

    // Удаление заказа
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long orderId) {
        orderService.delete(userId, orderId);
        return ResponseEntity.noContent().build();
    }
}
