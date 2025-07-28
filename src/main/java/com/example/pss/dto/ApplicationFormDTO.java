package com.example.pss.dto;

import com.example.pss.model.ApplicationForm;
import com.example.pss.model.ApplicationStatusEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationFormDTO {
    private Long id;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String idType;
    private String idNumber;
    private String registrationNumber; // NEW: Added registrationNumber
    private String contactEmail;
    private String educationLevel;
    private String previousSchool;
    private String selectedCenter;
    private List<Long> preferredCourses;
    private ApplicationStatusEnum applicationStatus;
    private Long adminSelectedCourseId;
    private String adminSelectedCenter;
    private LocalDateTime createdAt; // NEW: Added createdAt
    private LocalDateTime updatedAt;

    public ApplicationFormDTO() {
    }

    // Constructor to map from ApplicationForm entity
    public ApplicationFormDTO(ApplicationForm form) {
        this.id = form.getId();
        this.fullName = form.getFullName();
        this.dateOfBirth = form.getDateOfBirth();
        this.gender = form.getGender();
        this.nationality = form.getNationality();
        this.idType = form.getIdType();
        this.idNumber = form.getIdNumber();
        this.registrationNumber = form.getRegistrationNumber(); // Map registrationNumber
        this.contactEmail = form.getContactEmail();
        this.educationLevel = form.getEducationLevel();
        this.previousSchool = form.getPreviousSchool();
        this.selectedCenter = form.getSelectedCenter();
        this.preferredCourses = form.getPreferredCourses();
        this.applicationStatus = form.getApplicationStatus();
        this.adminSelectedCourseId = form.getAdminSelectedCourseId();
        this.adminSelectedCenter = form.getAdminSelectedCenter();
        this.createdAt = form.getCreatedAt(); // Map createdAt
        this.updatedAt = form.getUpdatedAt();
    }

    // Getters and Setters (omitted for brevity, assume all are present)
    // You can generate them with your IDE if not already present.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getPreviousSchool() {
        return previousSchool;
    }

    public void setPreviousSchool(String previousSchool) {
        this.previousSchool = previousSchool;
    }

    public String getSelectedCenter() {
        return selectedCenter;
    }

    public void setSelectedCenter(String selectedCenter) {
        this.selectedCenter = selectedCenter;
    }

    public List<Long> getPreferredCourses() {
        return preferredCourses;
    }

    public void setPreferredCourses(List<Long> preferredCourses) {
        this.preferredCourses = preferredCourses;
    }

    public ApplicationStatusEnum getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatusEnum applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Long getAdminSelectedCourseId() {
        return adminSelectedCourseId;
    }

    public void setAdminSelectedCourseId(Long adminSelectedCourseId) {
        this.adminSelectedCourseId = adminSelectedCourseId;
    }

    public String getAdminSelectedCenter() {
        return adminSelectedCenter;
    }

    public void setAdminSelectedCenter(String adminSelectedCenter) {
        this.adminSelectedCenter = adminSelectedCenter;
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
}
