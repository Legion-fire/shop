package com.example.shop.model;

import com.example.shop.view.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({OrderSummary.class})
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // предотвращаем рекурсию при сериализации
    private User user;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "item")
    @NotEmpty
    @JsonView({OrderSummary.class})
    private List<String> items = new ArrayList<>();

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @JsonView({OrderSummary.class})
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @NotNull
    @JsonView({OrderSummary.class})
    private OrderStatus status;

}
