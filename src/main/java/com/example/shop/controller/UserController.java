package com.example.shop.controller;

import com.example.shop.model.User;
import com.example.shop.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Получение списка всех пользователей (без деталей заказов)
    @GetMapping
    @JsonView(UserSummary.class)
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // Получение информации о конкретном пользователе (включая детали заказов)
    @GetMapping("/{id}")
    @JsonView(UserSummary.class)
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    // Создание нового пользователя
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User payload,
                                           UriComponentsBuilder uriBuilder) {
        User created = userService.create(payload);
        URI location = uriBuilder.path("/api/users/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Обновление информации о пользователе
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody User payload) {
        return userService.update(id, payload);
    }

    // Удаление пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
