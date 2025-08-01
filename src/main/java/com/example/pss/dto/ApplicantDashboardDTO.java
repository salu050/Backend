package com.example.pss.dto;

import java.time.LocalDateTime;
import java.util.List; // Import List for preferredCourses

public class ApplicantDashboardDTO {
    private Long id;
    private String fullName; // Changed from 'fullname' to 'fullName'
    private String username;
    private String registrationNumber;
    private String course;
    private String center;
    private String applicationStatus; // Changed from 'status' to 'applicationStatus'
    private LocalDateTime createdAt;
    private String educationLevel;
    private List<Long> preferredCourses;

    public ApplicantDashboardDTO() {
    }

    // UPDATED CONSTRUCTOR to include educationLevel and preferredCourses
    // And updated parameter names to match new field names
    public ApplicantDashboardDTO(Long id, String fullName, String username, String registrationNumber, String course,
            String center, String applicationStatus, LocalDateTime createdAt,
            String educationLevel, List<Long> preferredCourses) {
        this.id = id;
        this.fullName = fullName; // Initialize with new name
        this.username = username;
        this.registrationNumber = registrationNumber;
        this.course = course;
        this.center = center;
        this.applicationStatus = applicationStatus; // Initialize with new name
        this.createdAt = createdAt;
        this.educationLevel = educationLevel;
        this.preferredCourses = preferredCourses;
    }

    // Getters and setters (ensure all are present, especially for new fields)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() { // Changed from getFullname()
        return fullName;
    }

    public void setFullName(String fullName) { // Changed from setFullname()
        this.fullName = fullName;
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

    public String getApplicationStatus() { // Changed from getStatus()
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) { // Changed from setStatus()
        this.applicationStatus = applicationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

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
