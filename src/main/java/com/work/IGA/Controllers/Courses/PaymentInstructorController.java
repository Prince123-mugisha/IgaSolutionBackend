package com.work.IGA.Controllers.Courses;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.work.IGA.Models.Courses.Payment;
import com.work.IGA.Models.Courses.PaymentStatus;
import com.work.IGA.Services.CourseServices.PaymentService;
import com.work.IGA.Utils.PaymentUtils.PaymentAnalytics;
import com.work.IGA.Utils.PaymentUtils.PaymentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructor/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
public class PaymentInstructorController{
    
    private final PaymentService paymentService;

    // Get payment for instructor's courses 
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Payment>> getCoursePayments(@PathVariable UUID courseId) {
        try {
            List<Payment> payments = paymentService.getCoursePayments(courseId);
            return ResponseEntity.ok(payments);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all payments by status 
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(
        @PathVariable String status
    ) {

        try {
            List<Payment> payments = paymentService.getPaymentsByStatus(status);
            return ResponseEntity.ok(payments);

        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); 
        }
    }

    // Update payment status (for admin operations)
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
        @PathVariable UUID paymentId,
        @PathVariable PaymentStatus status
    ) {
        try {
            Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, status);
            return ResponseEntity.ok(updatedPayment);

        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    // Process refund for instructor's course payment 
    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<PaymentResponse> processRefund(
        @PathVariable UUID paymentId,
        @RequestParam String reason
     ) {
        try {
            PaymentResponse response = paymentService.processRefund(paymentId, reason);
            return ResponseEntity.ok(response);

        }
        catch(Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.errorResponse(
                        "Failed to process refund: " + e.getMessage(),
                        "REFUND_PROCESSING_ERROR"));
        }
     }

     // Get payment details by Id 
     @GetMapping("/{paymentId}")
     public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        try {
            Object response = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok((PaymentResponse) response);

        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.errorResponse(
                        "Failed to retrieve payment: " + e.getMessage(),
                        "PAYMENT_RETRIEVAL_ERROR"));
        }
     }

     // Get payments analytics for instructor's courses 
     @GetMapping("/analytics/course/courseId")
     public ResponseEntity<PaymentAnalytics> getCoursePaymentAnalytics(@PathVariable UUID courseId) {
        try {

            List<Payment> payments = paymentService.getCoursePayments(courseId);
            
            PaymentAnalytics analytics = PaymentAnalytics.builder()
                .totalPayments(payments.size())
                .completedPayments((int) payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                    .count())
                .pendingPayments((int) payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                    .count())
                .failedPayments((int) payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.FAILED)
                    .count())
                .totalRevenue(payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                    .mapToDouble(Payment::getAmount)
                    .sum())
                .build();
            
            return ResponseEntity.ok(analytics);

        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
     }

}
