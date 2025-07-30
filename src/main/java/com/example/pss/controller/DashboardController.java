package com.example.pss.controller;

import com.example.pss.dto.ApplicantDashboardDTO; // Keep this DTO if you are using it
import com.example.pss.model.ApplicationForm;
import com.example.pss.model.Course;
import com.example.pss.repository.ApplicationFormRepository;
import com.example.pss.repository.CourseRepository;
import com.example.pss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays; // Import Arrays for List - although not directly used in this method, it's fine
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
// FIX: Updated CrossOrigin to allow both HTTP and HTTPS from localhost:3000
@CrossOrigin(origins = { "http://localhost:3000", "https://localhost:3000" }, allowCredentials = "true") // Added
                                                                                                         // allowCredentials
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
     * Retrieves a list of applicants for the dashboard.
     * Accessible by users with either the 'ADMIN' or 'MINISTRY' role.
     *
     * @return ResponseEntity with a list of ApplicantDashboardDTOs.
     */
    @GetMapping("/applicants")
    // FIX: Allow both 'ADMIN' and 'MINISTRY' roles to access this endpoint
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY')") // <--- THIS IS THE CRUCIAL CHANGE
    public ResponseEntity<List<ApplicantDashboardDTO>> getDashboardApplicants() {
        // Assuming findAllWithUsers() correctly fetches ApplicationForm entities
        List<ApplicationForm> applications = applicationFormRepository.findAllWithUsers();
        List<ApplicantDashboardDTO> result = new ArrayList<>();

        for (ApplicationForm app : applications) {
            String username = "N/A";
            if (app.getUser() != null) {
                username = app.getUser().getUsername();
            }

            String courseNameForDisplay = "";
            // Logic to determine which course name to display in the 'course' column.
            // If adminSelectedCourseId exists, use that. Otherwise, use the first preferred
            // course.
            if (app.getAdminSelectedCourseId() != null) {
                Optional<Course> courseOpt = courseRepository.findById(app.getAdminSelectedCourseId());
                courseNameForDisplay = courseOpt.map(Course::getName).orElse("N/A");
            } else if (app.getPreferredCourses() != null && !app.getPreferredCourses().isEmpty()) {
                // Assuming you want to display the first preferred course name if no admin
                // course is selected
                Optional<Course> courseOpt = courseRepository.findById(app.getPreferredCourses().get(0));
                courseNameForDisplay = courseOpt.map(Course::getName).orElse("N/A") + " (Preferred)";
            }

            String registrationNumber = app.getRegistrationNumber() != null ? app.getRegistrationNumber() : "N/A";
            LocalDateTime submissionDate = app.getCreatedAt();

            // --- Crucial additions for Education Level, Center, and Preferred Courses list
            String educationLevel = app.getEducationLevel() != null ? app.getEducationLevel() : "N/A";
            String selectedCenter = app.getSelectedCenter() != null ? app.getSelectedCenter() : "N/A";
            List<Long> preferredCoursesList = app.getPreferredCourses(); // Directly get the list of IDs

            result.add(new ApplicantDashboardDTO(
                    app.getId(),
                    app.getFullName(),
                    username,
                    registrationNumber,
                    courseNameForDisplay, // Pass the chosen course name for the 'Course' column
                    selectedCenter, // Pass the actual selected center
                    app.getApplicationStatus() != null ? app.getApplicationStatus().name() : "N/A",
                    submissionDate,
                    educationLevel, // Pass the education level
                    preferredCoursesList // Pass the list of preferred course IDs
            ));
        }

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        }
        return ResponseEntity.ok(result); // Return 200 OK with the list
    }
}
