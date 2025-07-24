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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final ApplicationFormRepository applicationFormRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository; // Keep UserRepository for other potential uses if needed

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
        // Use the new method to fetch applications along with their users to avoid N+1
        // queries
        List<ApplicationForm> applications = applicationFormRepository.findAllWithUsers();
        List<ApplicantDashboardDTO> result = new ArrayList<>();

        for (ApplicationForm app : applications) {
            String username = "N/A";
            // Access username directly from the User object associated with the application
            if (app.getUser() != null) { // Check if the user object is present (should be with JOIN FETCH)
                username = app.getUser().getUsername();
            }

            String courseName = "";
            // Use getAdminSelectedCourseId() for the course selected by admin for display
            // on dashboard
            // If you want to show the student's preferred course, use getPreferredCourses()
            if (app.getAdminSelectedCourseId() != null) {
                Optional<Course> courseOpt = courseRepository.findById(app.getAdminSelectedCourseId());
                courseName = courseOpt.map(Course::getName).orElse("N/A");
            } else if (app.getPreferredCourses() != null && !app.getPreferredCourses().isEmpty()) {
                // Fallback to showing the first preferred course if admin hasn't selected yet
                Optional<Course> courseOpt = courseRepository.findById(app.getPreferredCourses().get(0));
                courseName = courseOpt.map(Course::getName).orElse("N/A") + " (Preferred)";
            }

            // Ensure getApplicationStatus() returns the enum, then use .name() to get its
            // string value
            result.add(new ApplicantDashboardDTO(
                    app.getId(),
                    app.getFullName(),
                    username,
                    courseName,
                    app.getSelectedCenter(), // This is the student's preferred center
                    app.getApplicationStatus() != null ? app.getApplicationStatus().name() : "N/A" // Convert enum to
                                                                                                   // string
            ));
        }

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        }
        return ResponseEntity.ok(result); // Return 200 OK with the list
    }
}
