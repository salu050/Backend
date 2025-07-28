package com.example.pss.controller;

import com.example.pss.dto.ApplicantDashboardDTO;
import com.example.pss.model.ApplicationForm;
import com.example.pss.model.Course;
import com.example.pss.model.User; // Import User model
import com.example.pss.repository.ApplicationFormRepository;
import com.example.pss.repository.CourseRepository;
import com.example.pss.repository.UserRepository; // Import UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // Import LocalDateTime
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final ApplicationFormRepository applicationFormRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Autowired
    public DashboardController(ApplicationFormRepository applicationFormRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.applicationFormRepository = applicationFormRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of applicants for the admin dashboard.
     * Accessible only by users with the 'ADMIN' role.
     *
     * @return ResponseEntity with a list of ApplicantDashboardDTOs.
     */
    @GetMapping("/applicants")
    @PreAuthorize("hasRole('ADMIN')") // Secure this endpoint for ADMINs only
    public ResponseEntity<List<ApplicantDashboardDTO>> getDashboardApplicants() {
        List<ApplicationForm> applications = applicationFormRepository.findAllWithUsers();
        List<ApplicantDashboardDTO> result = new ArrayList<>();

        for (ApplicationForm app : applications) {
            String username = "N/A";
            if (app.getUser() != null) {
                username = app.getUser().getUsername();
            }

            String courseName = "";
            if (app.getAdminSelectedCourseId() != null) {
                Optional<Course> courseOpt = courseRepository.findById(app.getAdminSelectedCourseId());
                courseName = courseOpt.map(Course::getName).orElse("N/A");
            } else if (app.getPreferredCourses() != null && !app.getPreferredCourses().isEmpty()) {
                Optional<Course> courseOpt = courseRepository.findById(app.getPreferredCourses().get(0));
                courseName = courseOpt.map(Course::getName).orElse("N/A") + " (Preferred)";
            }

            String registrationNumber = app.getRegistrationNumber() != null ? app.getRegistrationNumber() : "N/A";
            LocalDateTime submissionDate = app.getCreatedAt();

            result.add(new ApplicantDashboardDTO(
                    app.getId(),
                    app.getFullName(), // <<-- Ensure getFullName() is used here
                    username,
                    registrationNumber,
                    courseName,
                    app.getSelectedCenter(),
                    app.getApplicationStatus() != null ? app.getApplicationStatus().name() : "N/A",
                    submissionDate));
        }

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        }
        return ResponseEntity.ok(result); // Return 200 OK with the list
    }
}
