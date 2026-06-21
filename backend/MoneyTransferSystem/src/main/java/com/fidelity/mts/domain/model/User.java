package com.fidelity.mts.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /** BCrypt-hashed password — never stored in plain text. */
    @NotNull
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** The account created for this user at registration time. */
    @Column(name = "account_id", nullable = false, unique = true)
    private Long accountId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(String name, String email, String passwordHash, Long accountId) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountId = accountId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
