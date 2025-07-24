package com.example.pss.service;

import com.example.pss.model.Payment;
import com.example.pss.model.User;
import com.example.pss.repository.PaymentRepository;
import com.example.pss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all payment records.
     * Now explicitly uses findAllWithUsers() to ensure associated User details are
     * fetched.
     * 
     * @return A list of all Payment entities with their associated User details.
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAllWithUsers(); // Changed to use the new JOIN FETCH query
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUser_Id(userId);
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        Optional<Payment> existingPendingPayment = paymentRepository.findByUser_IdAndStatus(payment.getUser().getId(),
                "PENDING");
        if (existingPendingPayment.isPresent()) {
            throw new RuntimeException(
                    "A pending payment already exists for this user. Please wait for approval or try again later.");
        }

        String controlNumber = UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        payment.setControlNumber(controlNumber);
        payment.setStatus("PENDING");

        return paymentRepository.save(payment);
    }

    @Transactional
    public Optional<Payment> updatePaymentStatus(Long paymentId, String newStatus) {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.setStatus(newStatus);
            Payment updatedPayment = paymentRepository.save(payment);

            if ("APPROVED".equals(newStatus)) {
                userRepository.findById(payment.getUser().getId()).ifPresent(user -> {
                    user.setHasPaidApplicationFee(true);
                    userRepository.save(user);
                });
            } else if ("REJECTED".equals(newStatus)) {
                userRepository.findById(payment.getUser().getId()).ifPresent(user -> {
                    user.setHasPaidApplicationFee(false);
                    userRepository.save(user);
                });
            }
            return Optional.of(updatedPayment);
        }
        return Optional.empty();
    }

    @Transactional
    public Optional<Payment> approveLatestPendingPaymentByUserId(Long userId) {
        Optional<Payment> latestPending = paymentRepository.findFirstByUser_IdAndStatusOrderByCreatedAtDesc(userId,
                "PENDING");
        if (latestPending.isPresent()) {
            return updatePaymentStatus(latestPending.get().getId(), "APPROVED");
        }
        return Optional.empty();
    }

    // You can add this method if you need to fetch a single payment by its ID with
    // its user
    public Optional<Payment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }
}