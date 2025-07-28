package com.example.pss.controller;

import com.example.pss.dto.ApplicationFormDTO;
import com.example.pss.model.ApplicationForm;
import com.example.pss.model.Course;
import com.example.pss.model.User;
import com.example.pss.repository.CourseRepository;
import com.example.pss.service.ApplicationFormService;
import com.example.pss.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/applications")
public class ApplicationFormController {

    private final ApplicationFormService applicationFormService;
    private final CourseRepository courseRepository;
    private final UserService userService;

    @Autowired
    public ApplicationFormController(ApplicationFormService applicationFormService, CourseRepository courseRepository,
            UserService userService) {
        this.applicationFormService = applicationFormService;
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createApplication(@RequestBody ApplicationForm form, Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);

        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: User ID not found.");
        }

        Optional<User> authenticatedUserOpt = userService.findById(authenticatedUserId);
        if (authenticatedUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authenticated user not found in database.");
        }
        User authenticatedUser = authenticatedUserOpt.get();

        if (authenticatedUser.getApplicationDetails() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("You have already submitted an application.");
        }

        form.setUser(authenticatedUser);
        ApplicationForm saved = applicationFormService.saveApplication(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Retrieves a list of all application forms as DTOs.
     * Accessible only by users with 'ADMIN' or 'MINISTRY' roles.
     *
     * @return ResponseEntity with a list of ApplicationFormDTOs.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY')")
    public ResponseEntity<List<ApplicationFormDTO>> getAllApplications() {
        List<ApplicationForm> list = applicationFormService.getAllApplications();
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // --- THE FIX IS HERE ---
        // Map the list of ApplicationForm entities to a list of DTOs
        List<ApplicationFormDTO> dtoList = list.stream()
                .map(ApplicationFormDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (@applicationFormService.getApplicationById(#id).isPresent() and @applicationFormService.getApplicationById(#id).get().getUser().getId() == authentication.principal.id)")
    public ResponseEntity<ApplicationForm> getApplicationById(@PathVariable Long id, Authentication authentication) {
        Optional<ApplicationForm> found = applicationFormService.getApplicationById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found.get());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (#userId == authentication.principal.id)")
    public ResponseEntity<ApplicationForm> getByUserId(@PathVariable Long userId, Authentication authentication) {
        List<ApplicationForm> apps = applicationFormService.getByUserId(userId);
        if (apps.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(apps.get(0));
    }

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

        form.setUser(existing.get().getUser());
        form.setId(id);
        ApplicationForm updated = applicationFormService.saveApplication(form);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY')")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        boolean deleted = applicationFormService.deleteApplicationById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/select/{courseId}/{adminSelectedCenter}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationForm> selectStudent(@PathVariable Long id, @PathVariable Long courseId,
            @PathVariable String adminSelectedCenter) {
        Optional<ApplicationForm> updated = applicationFormService.selectStudent(id, courseId, adminSelectedCenter);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationForm> rejectStudent(@PathVariable Long id) {
        Optional<ApplicationForm> updated = applicationFormService.rejectStudent(id);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public static class ApplicationStatusDTO {
        public String application_status;
        public String center;
        public String selected_course_name;
        public String admin_selected_center;
        public String submission_date;
        public String last_updated;

        public ApplicationStatusDTO(ApplicationForm form, String selectedCourseName) {
            this.application_status = form.getApplicationStatus() != null ? form.getApplicationStatus().name() : null;
            this.center = form.getSelectedCenter();
            this.selected_course_name = selectedCourseName;
            this.admin_selected_center = form.getAdminSelectedCenter();
            this.submission_date = form.getCreatedAt() != null ? form.getCreatedAt().toString() : null;
            this.last_updated = form.getUpdatedAt() != null ? form.getUpdatedAt().toString()
                    : LocalDateTime.now().toString();
        }
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (@applicationFormService.getApplicationById(#id).isPresent() and @applicationFormService.getApplicationById(#id).get().getUser().getId() == authentication.principal.id)")
    public ResponseEntity<ApplicationStatusDTO> getApplicationStatus(@PathVariable Long id,
            Authentication authentication) {
        Optional<ApplicationForm> appOpt = applicationFormService.getApplicationById(id);
        if (appOpt.isEmpty())
            return ResponseEntity.notFound().build();
        ApplicationForm form = appOpt.get();

        String selectedCourseName = null;
        if (form.getAdminSelectedCourseId() != null) {
            selectedCourseName = courseRepository.findById(form.getAdminSelectedCourseId())
                    .map(Course::getName)
                    .orElse(null);
        }
        ApplicationStatusDTO dto = new ApplicationStatusDTO(form, selectedCourseName);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY') or (#userId == authentication.principal.id)")
    public ResponseEntity<ApplicationStatusDTO> getStatusByUserId(@PathVariable Long userId,
            Authentication authentication) {
        List<ApplicationForm> apps = applicationFormService.getByUserId(userId);
        if (apps.isEmpty())
            return ResponseEntity.notFound().build();
        ApplicationForm form = apps.get(0);

        String selectedCourseName = null;
        if (form.getAdminSelectedCourseId() != null) {
            selectedCourseName = courseRepository.findById(form.getAdminSelectedCourseId())
                    .map(Course::getName)
                    .orElse(null);
        }
        ApplicationStatusDTO dto = new ApplicationStatusDTO(form, selectedCourseName);
        return ResponseEntity.ok(dto);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        String username = authentication.getName();
        return userService.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}