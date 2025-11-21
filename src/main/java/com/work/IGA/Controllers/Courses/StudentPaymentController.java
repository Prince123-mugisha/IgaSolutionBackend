package com.work.IGA.Controllers.Courses;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.work.IGA.Models.Courses.Payment;
import com.work.IGA.Utils.PaymentUtils.PaymentHistoryDto;
import com.work.IGA.Services.CourseServices.PaymentService;
import com.work.IGA.Utils.PaymentUtils.PaymentException;
import com.work.IGA.Utils.PaymentUtils.PaymentRequest;
import com.work.IGA.Utils.PaymentUtils.PaymentResponse;
import com.work.IGA.Utils.PaymentUtils.PaymentVerificationRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/student/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentPaymentController {

    private final PaymentService paymentService;

    /**
     * Initiate payment for a course
     */
    @PostMapping("/initiate/course")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract token without Bearer prefix and set it in the request
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            paymentRequest.setToken(cleanToken);
            
            PaymentResponse response = paymentService.initiatePayment(paymentRequest);
            return ResponseEntity.ok(response);
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse.fromException(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.errorResponse(
                        "Failed to initiate payment: " + e.getMessage(),
                        "PAYMENT_INITIATION_ERROR"));
        }
    }

    /**
     * Verify payment status
     */
    @PostMapping("/course/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest verificationRequest) {
        try {
            PaymentResponse response = paymentService.verifyPayment(verificationRequest);
            return ResponseEntity.ok(response);
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse.fromException(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.errorResponse(
                        "Failed to verify payment: " + e.getMessage(),
                        "PAYMENT_VERIFICATION_ERROR"));
        }
    }

    /**
     * Get student's payment history with pagination
     */
    @GetMapping("/history")
    public ResponseEntity<Page<PaymentHistoryDto>> getPaymentHistory(
            @PageableDefault(size = 10, sort = "paymentDate") Pageable pageable,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract token without Bearer prefix
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            Page<PaymentHistoryDto> payments = paymentService.getUserPaymentHistoryByToken(cleanToken, pageable);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get specific payment details
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable UUID paymentId) {
        try {
            return paymentService.getPaymentById(paymentId)
                    .map(payment -> ResponseEntity.ok(payment))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get payment by reference
     */
    @GetMapping("/reference/{reference}")
    public ResponseEntity<Payment> getPaymentByReference(@PathVariable String reference) {
        try {
            return paymentService.getPaymentByReference(reference)
                    .map(payment -> ResponseEntity.ok(payment))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if student has paid for a specific course
     */
    @GetMapping("/check/{courseId}")
    public ResponseEntity<Boolean> checkPaymentStatus(
            @PathVariable UUID courseId,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract token without Bearer prefix
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean hasPaid = paymentService.hasUserPaidForCourseByToken(cleanToken, courseId);
            return ResponseEntity.ok(hasPaid);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }

    /**
     * Request refund for a payment
     */
    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<PaymentResponse> requestRefund(
            @PathVariable UUID paymentId,
            @RequestParam String reason,
            @RequestHeader("Authorization") String token) {
        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            PaymentResponse response = paymentService.requestRefund(paymentId, reason, cleanToken);
            return ResponseEntity.ok(response);
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse.fromException(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.errorResponse(
                        "Failed to process refund: " + e.getMessage(),
                        "REFUND_PROCESSING_ERROR"));
        }
    }
}