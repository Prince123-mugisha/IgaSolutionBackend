package com.work.IGA.Controllers.Courses;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.work.IGA.Models.Courses.Payment;
import com.work.IGA.Models.Courses.PaymentStatus;
import com.work.IGA.Services.CourseServices.PaymentService;
import com.work.IGA.Utils.PaymentUtils.PaymentException;
import com.work.IGA.Utils.PaymentUtils.PaymentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminPaymentController {
   
 private final PaymentService paymentService;

    /**
     * Get all payments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        try {
            List<Payment> payments = paymentService.getPaymentsByStatus(status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user payments (admin view)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Payment>> getUserPayments(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "paymentDate") Pageable pageable) {
        try {
            Page<Payment> payments = paymentService.getUserPayments(userId, pageable);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update payment status
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestParam PaymentStatus status) {
        try {
            Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, status);
            return ResponseEntity.ok(updatedPayment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process refund (admin action)
     */
    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<PaymentResponse> processRefund(
            @PathVariable UUID paymentId,
            @RequestParam String reason) {
        try {
            PaymentResponse response = paymentService.processRefund(paymentId, reason);
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

    /**
     * Get payment by ID
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
    
}
