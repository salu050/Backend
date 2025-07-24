package com.example.pss.model;

import jakarta.persistence.*; // Using jakarta.persistence.* for newer Spring Boot versions
import java.time.LocalDateTime;

@Entity
@Table(name = "payments") // Assuming your table is named 'payments'
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "control_number", unique = true, nullable = false)
    private String controlNumber; // Unique payment identifier

    @Column(nullable = false)
    private String status; // e.g., PENDING, APPROVED, REJECTED

    // --- THIS IS THE CRITICAL PART ---
    // Establish a Many-to-One relationship with the User entity.
    // Each Payment belongs to one User.
    // FetchType.EAGER means the associated User will be loaded immediately when a
    // Payment is loaded.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false) // This specifies the foreign key column in the 'payments' table
    private User user; // This will hold the associated User object, including its username

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING"; // Default status
    }

    // You might want a constructor that takes User as well for easier creation
    public Payment(Double amount, String controlNumber, String status, User user) {
        this.amount = amount;
        this.controlNumber = controlNumber;
        this.status = status;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getControlNumber() {
        return controlNumber;
    }

    public void setControlNumber(String controlNumber) {
        this.controlNumber = controlNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // --- IMPORTANT: Ensure you have getUser() and setUser(User user)
    // and NOT getUserId()/setUserId(Long userId) if you want the JPA relationship
    // to work.
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null || status.isEmpty()) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}