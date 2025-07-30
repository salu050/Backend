package com.example.pss.controller;

import com.example.pss.model.Payment;
import com.example.pss.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import for security annotation
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
// FIX: Updated CrossOrigin to allow both HTTP and HTTPS from localhost:3000
@CrossOrigin(origins = { "http://localhost:3000", "https://localhost:3000" })
public class AdminController {

    // Using constructor injection
    private final PaymentService paymentService;

    @Autowired
    public AdminController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Approves a payment by its unique payment ID.
     * Requires ADMIN role.
     */
    @PostMapping("/approve-payment")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can approve payments
    public ResponseEntity<?> approvePayment(@RequestParam Long paymentId) {
        Optional<Payment> updated = paymentService.updatePaymentStatus(paymentId, "APPROVED");
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        }
        return ResponseEntity.badRequest().body("Payment not found or could not be approved.");
    }

    /**
     * Rejects a payment by its unique payment ID.
     * Requires ADMIN role.
     */
    @PostMapping("/reject-payment")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can reject payments
    public ResponseEntity<?> rejectPayment(@RequestParam Long paymentId) {
        Optional<Payment> updated = paymentService.updatePaymentStatus(paymentId, "REJECTED");
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        }
        return ResponseEntity.badRequest().body("Payment not found or could not be rejected.");
    }

    /**
     * Approves the latest pending payment for a given user ID.
     * Requires ADMIN role.
     * This endpoint is primarily for internal/testing admin tools,
     * as the frontend admin dashboard typically approves by paymentId.
     */
    @PostMapping("/approve-payment-by-user")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can approve payments by user
    public ResponseEntity<?> approvePaymentByUser(@RequestParam Long userId) {
        Optional<Payment> updated = paymentService.approveLatestPendingPaymentByUserId(userId);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        }
        // Specific message for this case
        return ResponseEntity.badRequest()
                .body("No pending payment found for user with ID: " + userId + " or payment could not be approved.");
    }

    /**
     * Retrieves all payment records for administrative review.
     * Requires ADMIN role.
     *
     * @return A list of all Payment entities.
     */
    @GetMapping("/payments/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getAllPaymentsForAdmin() {
        List<Payment> payments = paymentService.getAllPayments();
        if (payments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(payments);
    }

    /**
     * Retrieves payment records for a specific user, for administrative review.
     * Requires ADMIN role.
     *
     * @param userId The ID of the user whose payments are to be retrieved.
     * @return A list of Payment entities for the specified user.
     */
    @GetMapping("/payments/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getPaymentsForUserByAdmin(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        if (payments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(payments);
    }
}
