package com.notifyflow.user.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private String role = "USER";

    @Builder.Default
    private boolean emailEnabled = true;

    @Builder.Default
    private boolean smsEnabled = false;

    @Builder.Default
    private boolean inAppEnabled = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
