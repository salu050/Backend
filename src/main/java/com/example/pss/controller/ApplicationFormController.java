package com.example.pss.controller;

import com.example.pss.model.ApplicationForm;
import com.example.pss.model.Course;
import com.example.pss.model.User; // Import User model
import com.example.pss.repository.CourseRepository;
import com.example.pss.service.ApplicationFormService;
import com.example.pss.service.UserService; // Import UserService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/applications")
public class ApplicationFormController {

    private final ApplicationFormService applicationFormService;
    private final CourseRepository courseRepository;
    private final UserService userService; // Inject UserService

    @Autowired
    public ApplicationFormController(ApplicationFormService applicationFormService, CourseRepository courseRepository,
            UserService userService) {
        this.applicationFormService = applicationFormService;
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    /**
     * Creates a new application form.
     * Accessible only by users with the 'STUDENT' role.
     * Ensures the application is created for the authenticated user.
     *
     * @param form           The ApplicationForm entity to be created.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with the created ApplicationForm object if successful,
     *         or 400/403 on error.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createApplication(@RequestBody ApplicationForm form, Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);

        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: User ID not found.");
        }

        // Fetch the actual User object to establish the relationship
        Optional<User> authenticatedUserOpt = userService.findById(authenticatedUserId);
        if (authenticatedUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authenticated user not found in database.");
        }
        User authenticatedUser = authenticatedUserOpt.get();

        // Check if the user already has an application via the User's
        // applicationDetails field
        if (authenticatedUser.getApplicationDetails() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("You have already submitted an application.");
        }

        // Set the User object on the ApplicationForm
        form.setUser(authenticatedUser); // CRITICAL: Establish the bidirectional relationship
        // The userId field in ApplicationForm is now redundant if using @OneToOne User
        // user;
        // Ensure your ApplicationForm model's getUserId() method returns user.getId()
        // if it's used in @PreAuthorize

        ApplicationForm saved = applicationFormService.saveApplication(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved); // Use 201 Created
    }

    /**
     * Retrieves a list of all application forms.
     * Accessible only by users with 'ADMIN' or 'MINISTRY' roles.
     *
     * @return ResponseEntity with a list of ApplicationForm entities.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY')")
    public ResponseEntity<List<ApplicationForm>> getAllApplications() {
        List<ApplicationForm> list = applicationFormService.getAllApplications();
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves an application form by its ID.
     * Accessible by 'ADMIN' or 'MINISTRY' roles, or by the 'STUDENT' if it's their
     * own application.
     *
     * @param id             The ID of the application form.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with the ApplicationForm entity, or 404/403.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (@applicationFormService.getApplicationById(#id).isPresent() and @applicationFormService.getApplicationById(#id).get().getUser().getId() == authentication.principal.id)")
    public ResponseEntity<ApplicationForm> getApplicationById(@PathVariable Long id, Authentication authentication) {
        Optional<ApplicationForm> found = applicationFormService.getApplicationById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // The @PreAuthorize handles the authorization logic, so we just return if
        // found.
        return ResponseEntity.ok(found.get());
    }

    /**
     * Retrieves application records associated with a specific user ID.
     * Accessible by 'ADMIN' or 'MINISTRY' roles (for any user) or by the 'STUDENT'
     * themselves (for their own application).
     *
     * @param userId         The ID of the user whose application is being
     *                       requested.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with an ApplicationForm entity (assuming one per
     *         user), or 404/403.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (#userId == authentication.principal.id)")
    public ResponseEntity<ApplicationForm> getByUserId(@PathVariable Long userId, Authentication authentication) {
        List<ApplicationForm> apps = applicationFormService.getByUserId(userId);
        if (apps.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Assuming there's only one active application per user for simplicity, return
        // the first one.
        return ResponseEntity.ok(apps.get(0));
    }

    /**
     * Updates an existing application form.
     * Accessible only by users with the 'STUDENT' role.
     * Ensures the application being updated belongs to the authenticated user.
     *
     * @param id             The ID of the application form to update.
     * @param form           The updated ApplicationForm entity.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with the updated ApplicationForm, or 404/403.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') and @applicationFormService.getApplicationById(#id).isPresent() and @applicationFormService.getApplicationById(#id).get().getUser().getId() == authentication.principal.id")
    public ResponseEntity<?> updateApplication(@PathVariable Long id, @RequestBody ApplicationForm form,
            Authentication authentication) {
        Optional<ApplicationForm> existing = applicationFormService.getApplicationById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: User ID not found.");
        }

        // Ensure the User object is set on the form from the existing application
        // This maintains the bidirectional relationship and ensures the correct user is
        // linked
        form.setUser(existing.get().getUser());
        form.setId(id); // Ensure the ID from the path is used

        ApplicationForm updated = applicationFormService.saveApplication(form);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an application form by its ID.
     * Accessible only by users with 'ADMIN' or 'MINISTRY' roles.
     *
     * @param id The ID of the application form to delete.
     * @return ResponseEntity with no content if successful, or 404.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY')")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        boolean deleted = applicationFormService.deleteApplicationById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Selects a student for a specific course and center.
     * Accessible only by users with the 'ADMIN' role.
     *
     * @param id                  The ID of the application form to update.
     * @param courseId            The ID of the course to select the student for.
     * @param adminSelectedCenter The name of the center selected by admin.
     * @return ResponseEntity with the updated ApplicationForm, or 404.
     */
    @PostMapping("/{id}/select/{courseId}/{adminSelectedCenter}") // NEW: Added adminSelectedCenter to path
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationForm> selectStudent(@PathVariable Long id, @PathVariable Long courseId,
            @PathVariable String adminSelectedCenter) {
        Optional<ApplicationForm> updated = applicationFormService.selectStudent(id, courseId, adminSelectedCenter); // Pass
                                                                                                                     // adminSelectedCenter
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Rejects a student's application.
     * Accessible only by users with the 'ADMIN' role.
     *
     * @param id The ID of the application form to update.
     * @return ResponseEntity with the updated ApplicationForm, or 404.
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationForm> rejectStudent(@PathVariable Long id) {
        Optional<ApplicationForm> updated = applicationFormService.rejectStudent(id);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DTO for returning application status.
     * Includes admin-selected center and course name.
     */
    public static class ApplicationStatusDTO {
        public String application_status;
        public String center; // Student's preferred center
        public String selected_course_name; // Admin selected course name
        public String admin_selected_center; // Admin selected center name
        public String submission_date;
        public String last_updated;

        public ApplicationStatusDTO(ApplicationForm form, String selectedCourseName) {
            this.application_status = form.getApplicationStatus() != null ? form.getApplicationStatus().name() : null; // Use
                                                                                                                       // .name()
                                                                                                                       // for
                                                                                                                       // enum
            this.center = form.getSelectedCenter(); // Student's preferred center
            this.selected_course_name = selectedCourseName; // Admin selected course name
            this.admin_selected_center = form.getAdminSelectedCenter(); // Admin selected center from form

            this.submission_date = form.getCreatedAt() != null ? form.getCreatedAt().toString() : null;
            this.last_updated = form.getUpdatedAt() != null ? form.getUpdatedAt().toString()
                    : LocalDateTime.now().toString();
        }
    }

    /**
     * Retrieves the status of an application by its ID.
     * Accessible by 'ADMIN' or 'MINISTRY' roles, or by the 'STUDENT' if it's their
     * own application.
     *
     * @param id             The ID of the application form.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with ApplicationStatusDTO, or 404/403.
     */
    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (@applicationFormService.getApplicationById(#id).isPresent() and @applicationFormService.getApplicationById(#id).get().getUser().getId() == authentication.principal.id)")
    public ResponseEntity<ApplicationStatusDTO> getApplicationStatus(@PathVariable Long id,
            Authentication authentication) {
        Optional<ApplicationForm> appOpt = applicationFormService.getApplicationById(id);
        if (appOpt.isEmpty())
            return ResponseEntity.notFound().build();
        ApplicationForm form = appOpt.get();

        String selectedCourseName = null;
        if (form.getAdminSelectedCourseId() != null) { // Use admin selected course
            selectedCourseName = courseRepository.findById(form.getAdminSelectedCourseId())
                    .map(Course::getName)
                    .orElse(null);
        }
        ApplicationStatusDTO dto = new ApplicationStatusDTO(form, selectedCourseName);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieves the status of an application by user ID.
     * Accessible by 'ADMIN' or 'MINISTRY' roles, or by the 'STUDENT' themselves.
     *
     * @param userId         The ID of the user.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with ApplicationStatusDTO, or 404/403.
     */
    @GetMapping("/user/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (#userId == authentication.principal.id)")
    public ResponseEntity<ApplicationStatusDTO> getStatusByUserId(@PathVariable Long userId,
            Authentication authentication) {
        List<ApplicationForm> apps = applicationFormService.getByUserId(userId);
        if (apps.isEmpty())
            return ResponseEntity.notFound().build();
        ApplicationForm form = apps.get(0); // Assuming one application per user, or logic to get latest

        String selectedCourseName = null;
        if (form.getAdminSelectedCourseId() != null) { // Use admin selected course
            selectedCourseName = courseRepository.findById(form.getAdminSelectedCourseId())
                    .map(Course::getName)
                    .orElse(null);
        }
        ApplicationStatusDTO dto = new ApplicationStatusDTO(form, selectedCourseName);
        return ResponseEntity.ok(dto);
    }

    /**
     * Helper method to extract the authenticated user's ID.
     * This method is robust, first trying to cast to a custom UserDetails (if
     * applicable),
     * then falling back to fetching the user from the database by username.
     *
     * @param authentication The Spring Security Authentication object.
     * @return The ID of the authenticated user, or null if not found.
     */
    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        // Assuming your User model is directly used as principal by
        // CustomUserDetailsService,
        // you can cast it to your User model.
        if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        // Fallback: Retrieve user by username from the database if principal is not
        // directly User object
        String username = authentication.getName(); // Gets the username (principal name)
        return userService.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}