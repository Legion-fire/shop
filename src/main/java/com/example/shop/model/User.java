package com.example.shop.model;

import com.example.shop.view.*;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User implements UserDetails {

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


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toUnmodifiableSet());
    }
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "failed_attempt", nullable = false)
    private int failedAttempt;

    @Column(name = "lock_time")
    private Instant lockTime;

     public static final Duration LOCK_DURATION = Duration.ofMinutes(15);


    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public boolean unlockIfExpired(Duration lockDuration) {
        if (!accountNonLocked && lockTime != null
                && Instant.now().isAfter(lockTime.plus(lockDuration))) {
            accountNonLocked = true;
            failedAttempt = 0;
            lockTime = null;
            return true;
        }
        return false;
    }

}
