package com.example.pss.dto;

import java.time.LocalDateTime; // Import LocalDateTime

public class ApplicantDashboardDTO {
    private Long id;
    private String fullname;
    private String username;
    private String registrationNumber;
    private String course;
    private String center;
    private String status;
    private LocalDateTime createdAt; // NEW: Added createdAt for submission date

    public ApplicantDashboardDTO() {
    }

    public ApplicantDashboardDTO(Long id, String fullname, String username, String registrationNumber, String course,
            String center,
            String status, LocalDateTime createdAt) { // UPDATED: Added createdAt to constructor
        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.registrationNumber = registrationNumber;
        this.course = course;
        this.center = center;
        this.status = status;
        this.createdAt = createdAt; // NEW: Initialize createdAt
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // NEW: Getter for createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // NEW: Setter for createdAt
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
