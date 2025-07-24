package com.example.pss.dto;

public class ApplicantDashboardDTO {
    private Long id;
    private String fullname;
    private String username;
    private String course;
    private String center;
    private String status;

    public ApplicantDashboardDTO() {
    }

    public ApplicantDashboardDTO(Long id, String fullname, String username, String course, String center,
            String status) {
        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.course = course;
        this.center = center;
        this.status = status;
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
}