package com.example.pss.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "application_details")
public class ApplicationForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NEW: Link back to User
    @OneToOne(fetch = FetchType.LAZY) // LAZY fetch is generally better for performance for the back-reference
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true) // Ensure one-to-one and unique link
    @JsonBackReference // This side is the "back" of the reference, prevents infinite recursion
    private User user;

    // Existing form fields
    private String fullName;
    private String dateOfBirth; // Consider using LocalDate or Date for better type safety
    private String gender;
    private String nationality;
    private String idType;
    private String idNumber;
    private String contactPhone;
    private String contactEmail;
    private String educationLevel;
    private String previousSchool;
    private String selectedCenter;

    @ElementCollection // For storing a collection of simple types (like Longs for course IDs)
    @CollectionTable(name = "application_preferred_courses", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "course_id")
    private List<Long> preferredCourses; // Store as list of course IDs

    // NEW: Status and Admin Selection fields
    @Enumerated(EnumType.STRING) // Store enum name as string
    @Column(nullable = false)
    private ApplicationStatusEnum applicationStatus; // e.g., SUBMITTED, UNDER_REVIEW, SELECTED, REJECTED

    private Long adminSelectedCourseId; // Admin's selected course ID
    private String adminSelectedCenter; // Admin's selected center name

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public ApplicationForm() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.applicationStatus = ApplicationStatusEnum.SUBMITTED; // Default status on creation
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // NEW: Getter and Setter for User
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Existing Getters and Setters for form fields
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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    // NEW: Getters and Setters for status and admin selection
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

    // Add a pre-persist and pre-update listener to manage timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
