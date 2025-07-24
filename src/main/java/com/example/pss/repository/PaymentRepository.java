package com.example.pss.repository;

import com.example.pss.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds payments by the ID of the associated User.
     * This method leverages the 'user' field in the Payment entity
     * and accesses its 'id' property.
     * 
     * @param userId The ID of the user.
     * @return A list of payments associated with the user.
     */
    List<Payment> findByUser_Id(Long userId);

    /**
     * Finds a payment by the ID of the associated User and status.
     * This is typically used to check for an existing PENDING payment.
     * 
     * @param userId The ID of the user.
     * @param status The status of the payment (e.g., "PENDING", "APPROVED").
     * @return An Optional containing the Payment if found, or empty.
     */
    Optional<Payment> findByUser_IdAndStatus(Long userId, String status);

    /**
     * Finds payments by the ID of the associated User and status,
     * ordered by creation date in descending order.
     * Useful for getting the latest pending payment.
     * 
     * @param userId The ID of the user.
     * @param status The status of the payment.
     * @return A list of payments, ordered by creation date descending.
     */
    List<Payment> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    /**
     * Finds the first (latest) payment by user ID and status, ordered by creation
     * date in descending order.
     * Returns an Optional containing the single latest payment, if any.
     * This is often preferred over returning a List when you expect only one or
     * zero results.
     * 
     * @param userId The ID of the user.
     * @param status The status of the payment.
     * @return An Optional containing the latest Payment if found, or empty.
     */
    Optional<Payment> findFirstByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    /**
     * Finds a payment by its unique control number.
     * 
     * @param controlNumber The unique control number of the payment.
     * @return An Optional containing the Payment if found, or empty.
     */
    Optional<Payment> findByControlNumber(String controlNumber);

    // --- NEW METHOD FOR EAGER FETCHING USERS WITH ALL PAYMENTS ---
    /**
     * Retrieves all payment records along with their associated User entities.
     * Uses a JOIN FETCH query to ensure the User data is loaded in a single query,
     * preventing N+1 problems and ensuring the 'user' object is populated.
     * 
     * @return A list of all Payment entities with their associated User details.
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.user")
    List<Payment> findAllWithUsers();
}
