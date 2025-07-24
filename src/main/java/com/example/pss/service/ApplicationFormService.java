package com.example.pss.service;

import com.example.pss.model.ApplicationForm;
import com.example.pss.model.ApplicationStatusEnum;
import java.util.List;
import java.util.Optional;

public interface ApplicationFormService {
    ApplicationForm saveApplication(ApplicationForm form);

    List<ApplicationForm> getAllApplications();

    Optional<ApplicationForm> getApplicationById(Long id);

    boolean deleteApplicationById(Long id);

    Optional<ApplicationForm> selectStudent(Long id, Long courseId, String adminSelectedCenter); // Updated signature

    Optional<ApplicationForm> rejectStudent(Long id);

    List<ApplicationForm> getByUserId(Long userId);
}
