package com.example.pss.repository;

import com.example.pss.model.ApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, Long> {
    List<ApplicationForm> findByUserId(Long userId); // This method is still useful for direct user queries

    // Custom query to fetch all applications along with their associated User
    // objects
    @Query("SELECT af FROM ApplicationForm af JOIN FETCH af.user")
    List<ApplicationForm> findAllWithUsers();
}
