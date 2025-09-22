package com.example.shop.model;

import com.example.shop.view.*;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({UserSummary.class})
    private Long id;

    @NotBlank
    @JsonView({UserSummary.class})
    private String name;

    @NotBlank
    @Email
    @JsonView({UserSummary.class})
    private String email;

    @JsonView({UserSummary.class})
    private String address;

}
