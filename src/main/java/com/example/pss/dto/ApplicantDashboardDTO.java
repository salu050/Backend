package com.example.pss.dto;

import java.time.LocalDateTime;
import java.util.List; // Import List for preferredCourses

public class ApplicantDashboardDTO {
    private Long id;
    private String fullname;
    private String username;
    private String registrationNumber;
    private String course;
    private String center;
    private String status;
    private LocalDateTime createdAt;
    private String educationLevel; // <<< THIS IS NEW AND REQUIRED
    private List<Long> preferredCourses; // <<< THIS IS NEW AND REQUIRED

    public ApplicantDashboardDTO() {
    }

    // UPDATED CONSTRUCTOR to include educationLevel and preferredCourses
    public ApplicantDashboardDTO(Long id, String fullname, String username, String registrationNumber, String course,
            String center, String status, LocalDateTime createdAt,
            String educationLevel, List<Long> preferredCourses) {
        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.registrationNumber = registrationNumber;
        this.course = course;
        this.center = center;
        this.status = status;
        this.createdAt = createdAt;
        this.educationLevel = educationLevel; // Initialize new field
        this.preferredCourses = preferredCourses; // Initialize new field
    }

    // Getters and setters (ensure all are present, especially for new fields)
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // NEW GETTERS AND SETTERS FOR EDUCATION LEVEL AND PREFERRED COURSES
    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public List<Long> getPreferredCourses() {
        return preferredCourses;
    }

    public void setPreferredCourses(List<Long> preferredCourses) {
        this.preferredCourses = preferredCourses;
    }
}
