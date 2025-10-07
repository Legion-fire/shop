package com.example.shop.controller;

import com.example.shop.model.Order;
import com.example.shop.model.OrderStatus;
import com.example.shop.model.User;
import com.example.shop.repo.OrderRepository;
import com.example.shop.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerJsonViewTests {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired
    PasswordEncoder encoder;

    Long userId;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        User u = new User();
        u.setName("Alice");
        u.setEmail("alice1994@gmail.com");
        u.setAddress("Moscow Red Street 1");
        u.setPassword(encoder.encode("test123"));
        u.setUsername("alice-killer");
        u = userRepository.save(u);
        userId = u.getId();

        Order o1 = new Order();
        o1.setUser(u);
        o1.setItems(List.of("Book", "Pen"));
        o1.setTotalAmount(new BigDecimal("15.50"));
        o1.setStatus(OrderStatus.NEW);
        orderRepository.save(o1);

        Order o2 = new Order();
        o2.setUser(u);
        o2.setItems(List.of("Laptop"));
        o2.setTotalAmount(new BigDecimal("1200.00"));
        o2.setStatus(OrderStatus.PAID);
        orderRepository.save(o2);
    }

    @Test
    void getAllUsers_shouldNotIncludeOrders() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice1994@gmail.com"))
                .andExpect(jsonPath("$[0].orders").doesNotExist());
    }

    @Test
    void getUserById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getOrder_should() throws Exception {
        Long orderId = orderRepository.findByUserId(userId).get(0).getId();
        mockMvc.perform(get("/api/users/{uid}/orders/{oid}", userId, orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void createUser_invalidEmail_shouldReturn400() throws Exception {
        String body = """
      {"name":"Bob","email":"bad-email","address":"Street 1"}
      """;
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email", containsString("must be a well-formed email")));
    }
}
