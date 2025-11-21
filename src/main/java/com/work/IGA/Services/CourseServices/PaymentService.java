package com.work.IGA.Services.CourseServices;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;

import com.work.IGA.Models.Courses.Payment;
import com.work.IGA.Models.Courses.PaymentStatus;
import com.work.IGA.Utils.PaymentUtils.PaymentException;
import com.work.IGA.Utils.PaymentUtils.PaymentRequest;
import com.work.IGA.Utils.PaymentUtils.PaymentResponse;
import com.work.IGA.Utils.PaymentUtils.PaymentVerificationRequest;

public interface PaymentService {
   
      /**
     * Initiate payment for a course
     * @param paymentRequest - Payment initiation request
     * @return PaymentResponse with payment link and details
     * @throws PaymentException if initiation fails
     */

     PaymentResponse initiatePayment(PaymentRequest paymentRequest) throws PaymentException;

      /**
     * Verify payment with Flutterwave
     * @param verificationRequest - Payment verification request
     * @return PaymentResponse with verification status
     * @throws PaymentException if verification fails
     */

     PaymentResponse verifyPayment(PaymentVerificationRequest verificationRequest) throws PaymentException;

       /**
     * Handle payment webhook callback
     * @param transactionId - Flutterwave transaction ID
     * @param reference - Payment reference
     * @param status - Payment status from webhook
     * @return PaymentResponse with processing result
     * @throws PaymentException if webhook processing fails
     */
      PaymentResponse handlePaymentWebhook(String transactionId, String reference, String status) throws PaymentException;


       /**
     * Get payment by ID
     * @param paymentId - Payment UUID
     * @return Optional Payment entity
     */
    Optional<Payment> getPaymentById(UUID paymentId);
   
    /**
     * Get payment by reference
     * @param reference - Payment reference
     * @return Optional Payment entity
     */
    Optional<Payment> getPaymentByReference(String reference);
    
    /**
     * Get user payments with pagination
     * @param userId - User UUID
     * @param pageable - Pagination information
     * @return Page of payments
     */

     Page<Payment> getUserPayments(UUID userId, Pageable pageable);

     /**
     * Get course payments
     * @param courseId - Course UUID
     * @return List of payments for the course
     */
     List<Payment> getCoursePayments(UUID courseId);

     /**
     * Get payments by status
     * @param status - Payment status
     * @return List of payments with given status
     */
      List<Payment> getPaymentsByStatus(String status);
     
         /**
     * Update payment status
     * @param paymentId - Payment UUID
     * @param status - New payment status
     * @return Updated payment
     * @throws PaymentException if update fails
     */
   
     Payment updatePaymentStatus(UUID paymentId, PaymentStatus status);


      /**
     * Check if user has already paid for a course
     * @param userId - User UUID
     * @param courseId - Course UUID
     * @return true if payment exists and is completed
     */
      boolean hasUserPaidForCourse(UUID userId, UUID courseId);

      /**
     * Check if user has already paid for a course using JWT token
     * @param token - JWT token
     * @param courseId - Course UUID
     * @return true if payment exists and is completed
     */
      boolean hasUserPaidForCourseByToken(String token, UUID courseId);

        /**
     * Process refund for a payment
     * @param paymentId - Payment UUID
     * @param reason - Refund reason
     * @return PaymentResponse with refund status
     * @throws PaymentException if refund fails
     */
    PaymentResponse processRefund(UUID paymentId, String reason) throws PaymentException;

    Page<Payment> getUserPayments(UUID userId, org.springframework.data.domain.Pageable pageable);

    /**
     * Get user payments by JWT token with pagination (as DTOs)
     * @param token - JWT token
     * @param pageable - Pagination information
     * @return Page of payment DTOs for the authenticated user
     */
    Page<com.work.IGA.Utils.PaymentUtils.PaymentHistoryDto> getUserPaymentHistoryByToken(String token, org.springframework.data.domain.Pageable pageable);

    /**
     * Get user payments by JWT token with pagination
     * @param token - JWT token
     * @param pageable - Pagination information
     * @return Page of payments for the authenticated user
     */
    Page<Payment> getUserPaymentsByToken(String token, org.springframework.data.domain.Pageable pageable);

    PaymentResponse requestRefund(UUID paymentId, String reason , String token) throws PaymentException;
}