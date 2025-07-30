package com.example.pss.controller;

import com.example.pss.model.Payment;
import com.example.pss.model.User; // Import User model to work with user data
import com.example.pss.service.PaymentService;
import com.example.pss.service.UserService; // Import UserService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus for clearer status codes
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role-based authorization
import org.springframework.security.core.Authentication; // To get details of the authenticated user
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // For throwing HTTP status exceptions

// Import Arrays for List
import java.util.List;
import java.util.Optional;

// FIX: Updated CrossOrigin to allow both HTTP and HTTPS from localhost:3000
@CrossOrigin(origins = { "http://localhost:3000", "https://localhost:3000" })
@RestController
@RequestMapping("/api/payments") // Base path for payment-related endpoints
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @Autowired
    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    /**
     * Retrieves a list of all payment records.
     * Accessible only by users with the 'ADMIN' role.
     * This endpoint is typically used by the Admin Dashboard to fetch all payments.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    /**
     * Retrieves payment records associated with a specific user ID.
     * Accessible by 'ADMIN' role (for any user) or by the 'STUDENT' themselves (for
     * their own payments).
     *
     * @param userId         The ID of the user whose payments are being requested.
     * @param authentication The Spring Security Authentication object, containing
     *                       details of the current user.
     * @return ResponseEntity with a list of Payment entities, or appropriate HTTP
     *         status.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)") // Direct principal ID check
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Long userId, Authentication authentication) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        if (payments.isEmpty()) {
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        }
        return ResponseEntity.ok(payments);
    }

    /**
     * DTO for incoming payment creation requests from the frontend.
     * This helps in clearly defining the expected request body structure.
     */
    static class PaymentRequestDto {
        private Long userId;
        private Double amount;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }

    /**
     * Creates a new payment record.
     * Accessible only by users with the 'STUDENT' role.
     * Ensures the payment is created for the authenticated user.
     *
     * @param request        The PaymentRequestDto containing userId and amount from
     *                       the frontend.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with the created Payment object if successful, or
     *         400/403 on error.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequestDto request, Authentication authentication) {
        // Get the authenticated user's ID to ensure they are creating a payment for
        // themselves
        Long authenticatedUserId = getAuthenticatedUserId(authentication);

        if (authenticatedUserId == null || !authenticatedUserId.equals(request.getUserId())) {
            // If the authenticated user ID doesn't match the userId in the payment request,
            // deny.
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Unauthorized: Payment must be for the authenticated user.");
        }

        try {
            // Fetch the User entity to properly set the relationship in the Payment object
            User user = userService.findById(request.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User not found for payment creation."));

            // Create a new Payment entity and set the fetched User object
            Payment newPayment = new Payment();
            newPayment.setAmount(request.getAmount());
            newPayment.setUser(user); // Set the full User object here!
            // controlNumber, status, createdAt, updatedAt will be set by PaymentService or
            // @PrePersist

            Payment created = paymentService.createPayment(newPayment);
            return ResponseEntity.status(HttpStatus.CREATED).body(created); // HTTP 201 Created on success
        } catch (ResponseStatusException e) {
            // Catch ResponseStatusExceptions thrown (e.g., User not found)
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (RuntimeException e) {
            // Catch other specific exceptions from service (e.g., duplicate control number,
            // already approved)
            return ResponseEntity.badRequest().body(e.getMessage()); // HTTP 400 Bad Request on error
        }
    }

    /**
     * Retrieves the `hasPaidApplicationFee` status for the currently authenticated
     * user.
     * This is the endpoint your React dashboard and payment page will call to
     * update their status.
     * Accessible by any authenticated user.
     *
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with a boolean indicating payment status, or 404 if
     *         user not found (unlikely).
     */
    @GetMapping("/me/status")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can check their own status
    public ResponseEntity<Boolean> getAuthenticatedUserPaymentStatus(Authentication authentication) {
        // Get the username of the currently authenticated user from the Authentication
        // object
        String username = authentication.getName();

        // Use UserService to find the User entity by username
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            // Return the value of the `hasPaidApplicationFee` field
            return ResponseEntity.ok(userOptional.get().isHasPaidApplicationFee());
        }
        // This case should ideally not happen if Spring Security correctly
        // authenticated the user,
        // as a valid username implies a user exists. Included for robustness.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false); // User not found, return false
    }

    /**
     * Admin endpoint to approve a specific payment by ID.
     * This will also update the user's `hasPaidApplicationFee` status.
     * Accessible only by users with the 'ADMIN' role.
     *
     * @param paymentId The ID of the payment to approve.
     * @return ResponseEntity with the updated Payment object, or 404 if not found.
     */
    @PostMapping("/admin/approve-payment") // Changed to POST as it's an action
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approvePayment(@RequestParam Long paymentId) {
        Optional<Payment> updatedPayment = paymentService.updatePaymentStatus(paymentId, "APPROVED");
        if (updatedPayment.isPresent()) {
            return ResponseEntity.ok(updatedPayment.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found or could not be approved.");
    }

    /**
     * Admin endpoint to reject a specific payment by ID.
     * This will also update the user's `hasPaidApplicationFee` status to false.
     * Accessible only by users with the 'ADMIN' role.
     *
     * @param paymentId The ID of the payment to reject.
     * @return ResponseEntity with the updated Payment object, or 404 if not found.
     */
    @PostMapping("/admin/reject-payment") // Changed to POST as it's an action
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectPayment(@RequestParam Long paymentId) {
        Optional<Payment> updatedPayment = paymentService.updatePaymentStatus(paymentId, "REJECTED");
        if (updatedPayment.isPresent()) {
            return ResponseEntity.ok(updatedPayment.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found or could not be rejected.");
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

        // Option 2 (More general fallback): Retrieve user by username from the
        // database.
        String username = authentication.getName(); // Gets the username (principal name)
        return userService.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}
