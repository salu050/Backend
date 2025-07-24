package com.example.pss.service;

import com.example.pss.model.ApplicationForm;
import com.example.pss.model.ApplicationStatusEnum; // Import ApplicationStatusEnum
import com.example.pss.model.User; // Import User model
import com.example.pss.repository.ApplicationFormRepository;
import com.example.pss.repository.UserRepository; // Import UserRepository to fetch User
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import for @Transactional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationFormServiceImpl implements ApplicationFormService {

    @Autowired
    private ApplicationFormRepository applicationFormRepository;

    @Autowired
    private UserRepository userRepository; // Inject UserRepository

    @Override
    @Transactional // Ensure this operation is atomic for database consistency
    public ApplicationForm saveApplication(ApplicationForm form) {
        // Set creation and update timestamps if not already set
        if (form.getCreatedAt() == null) {
            form.setCreatedAt(LocalDateTime.now());
        }
        form.setUpdatedAt(LocalDateTime.now());

        // Set initial status for new applications
        if (form.getId() == null) { // This is a new application
            form.setApplicationStatus(ApplicationStatusEnum.SUBMITTED);
        }

        // Save the application form first
        ApplicationForm savedForm = applicationFormRepository.save(form);

        // Crucial: Update the associated User object to reflect the applicationDetails
        // This ensures that when the User is fetched (e.g., on login),
        // applicationDetails is present.
        if (savedForm.getUser() != null && savedForm.getUser().getId() != null) {
            Optional<User> userOpt = userRepository.findById(savedForm.getUser().getId());
            userOpt.ifPresent(user -> {
                // Set the applicationDetails on the User object
                user.setApplicationDetails(savedForm);
                userRepository.save(user); // Save the user to persist the link and updated applicationDetails
            });
        }
        return savedForm;
    }

    @Override
    public List<ApplicationForm> getAllApplications() {
        return applicationFormRepository.findAll();
    }

    @Override
    public Optional<ApplicationForm> getApplicationById(Long id) {
        return applicationFormRepository.findById(id);
    }

    @Override
    @Transactional
    public boolean deleteApplicationById(Long id) {
        Optional<ApplicationForm> appOpt = applicationFormRepository.findById(id);
        if (appOpt.isPresent()) {
            ApplicationForm formToDelete = appOpt.get();
            // If deleting an application, also remove the link from the User
            if (formToDelete.getUser() != null) {
                User user = formToDelete.getUser();
                user.setApplicationDetails(null); // Remove the link
                userRepository.save(user); // Save the user to update the relationship
            }
            applicationFormRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Optional<ApplicationForm> selectStudent(Long id, Long courseId, String adminSelectedCenter) {
        Optional<ApplicationForm> opt = applicationFormRepository.findById(id);
        if (opt.isPresent()) {
            ApplicationForm form = opt.get();
            form.setApplicationStatus(ApplicationStatusEnum.SELECTED); // Use enum
            form.setAdminSelectedCourseId(courseId); // Set admin selected course ID
            form.setAdminSelectedCenter(adminSelectedCenter); // Set admin selected center
            form.setUpdatedAt(LocalDateTime.now()); // Update timestamp

            ApplicationForm updatedForm = applicationFormRepository.save(form);

            // Update the associated User object with the new application details
            if (updatedForm.getUser() != null && updatedForm.getUser().getId() != null) {
                Optional<User> userOpt = userRepository.findById(updatedForm.getUser().getId());
                userOpt.ifPresent(user -> {
                    user.setApplicationDetails(updatedForm);
                    userRepository.save(user);
                });
            }
            return Optional.of(updatedForm);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<ApplicationForm> rejectStudent(Long id) {
        Optional<ApplicationForm> opt = applicationFormRepository.findById(id);
        if (opt.isPresent()) {
            ApplicationForm form = opt.get();
            form.setApplicationStatus(ApplicationStatusEnum.REJECTED); // Use enum
            form.setUpdatedAt(LocalDateTime.now()); // Update timestamp
            // Clear admin selected course/center if rejecting
            form.setAdminSelectedCourseId(null);
            form.setAdminSelectedCenter(null);

            ApplicationForm updatedForm = applicationFormRepository.save(form);

            // Update the associated User object with the new application details
            if (updatedForm.getUser() != null && updatedForm.getUser().getId() != null) {
                Optional<User> userOpt = userRepository.findById(updatedForm.getUser().getId());
                userOpt.ifPresent(user -> {
                    user.setApplicationDetails(updatedForm);
                    userRepository.save(user);
                });
            }
            return Optional.of(updatedForm);
        }
        return Optional.empty();
    }

    @Override
    public List<ApplicationForm> getByUserId(Long userId) {
        return applicationFormRepository.findByUserId(userId);
    }
}
