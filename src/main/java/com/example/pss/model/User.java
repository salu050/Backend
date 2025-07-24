package com.example.pss.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Hashed password

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Enum for roles: STUDENT, ADMIN, MINISTRY

    @Column(name = "has_paid_application_fee", nullable = false)
    private boolean hasPaidApplicationFee = false; // Default to false

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String token; // Field to hold the JWT token when returned from login

    // NEW: One-to-one relationship with ApplicationForm
    // mappedBy refers to the 'user' field in the ApplicationForm entity
    // CascadeType.ALL means operations (persist, merge, remove) on User will
    // cascade to ApplicationForm
    // FetchType.EAGER means ApplicationForm will be loaded immediately with User
    // when User is loaded
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference // This side manages the serialization and prevents infinite recursion
    private ApplicationForm applicationDetails; // Name this field 'applicationDetails' for frontend consistency

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override // From UserDetails
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override // From UserDetails
    public String getPassword() {
        return password; // This will be the hashed password
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isHasPaidApplicationFee() {
        return hasPaidApplicationFee;
    }

    public void setHasPaidApplicationFee(boolean hasPaidApplicationFee) {
        this.hasPaidApplicationFee = hasPaidApplicationFee;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // NEW: Getter and Setter for applicationDetails
    public ApplicationForm getApplicationDetails() {
        return applicationDetails;
    }

    public void setApplicationDetails(ApplicationForm applicationDetails) {
        this.applicationDetails = applicationDetails;
        // Ensure bidirectional relationship is set if applicationDetails is not null
        if (applicationDetails != null) {
            applicationDetails.setUser(this);
        }
    }

    // --- UserDetails Interface Implementations ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
