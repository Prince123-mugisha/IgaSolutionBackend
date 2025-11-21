package com.work.IGA.Utils.PaymentUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.work.IGA.Models.Courses.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder 
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    // Response metadata 
    private boolean success;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;

    // Payment initiation response data 
    private String paymentLink;
    private String paymentReference;
    private UUID paymentId;
    private String transactionId;

    // Payment details 
    private Double amount;
    private String currency;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;

    // Course and user information 
    private UUID courseId;
    private String courseName;
    private UUID userId;
    private String customerName;
    private String customerEmail;

    // Enrollment information (if payment successful)
    private UUID enrollmentId;
    private String enrollmentStatus;
    private LocalDateTime enrollmentDate;

    // Additional metadate 
    private Map<String, Object> metadata;

    // Gateway response data 
    private Object gatewayResponse;

    // Pagination data (for list responses)
    private PaginationData pagination;

    // Success response for payment initiation
    public static PaymentResponse initiationSuccess(String paymentLink, String reference, UUID paymentId, double amount) {
        return PaymentResponse.builder()
        .success(true)
        .message("Payment initiated successfully")
        .paymentLink(paymentLink)
        .paymentReference(reference)
        .paymentId(paymentId)
        .amount(amount)
        .paymentStatus(PaymentStatus.PENDING)
        .timestamp(LocalDateTime.now())
        .build();
    }


    // Success response for payment verification
    public static PaymentResponse verificationSuccess(UUID paymentId, UUID enrollmentId, String courseName) {
        return PaymentResponse.builder()
        .success(true)
        .message("Payment verified and enrollment completed successfully")
        .paymentId(paymentId)
        .enrollmentId(enrollmentId)
        .courseName(courseName)
        .paymentStatus(PaymentStatus.COMPLETED)
        .enrollmentStatus("ENROLLED")
        .enrollmentDate(LocalDateTime.now())
        .timestamp(LocalDateTime.now())
        .build();
    }

    // Error response
    public static PaymentResponse errorResponse(String message , String errorCode) {
        return PaymentResponse.builder()
        .success(false)
        .message(message)
        .errorCode(errorCode)
        .timestamp(LocalDateTime.now())
        .build();
    }
    
    
    // Error response from exception
    public static PaymentResponse fromException(PaymentException exception) {
        return  PaymentResponse.builder()
        .success(false)
        .message(exception.getUserMessage())
        .errorCode(exception.getErrorCode())
        .metadata(exception.getAdditionalData() != null ? 
        Map.of("additionalData", exception.getAdditionalData()) : null)
        .timestamp(LocalDateTime.now())
        .build();
    }

    // Payment failed response 
    public static PaymentResponse paymentFailed(String reference, String reason) {
        return PaymentResponse.builder()
        .success(false)
        .message("Payment failed : " + reason)
        .errorCode("PAYMENT_FAILED")
        .paymentReference(reference)
        .paymentStatus(PaymentStatus.FAILED)
        .timestamp(LocalDateTime.now())
        .build();
    }

    // Payment cacelled response 
    public static PaymentResponse paymentCacelled(String reference) {
        return PaymentResponse.builder()
        .success(false)
        .message("Payment was cancelled by user ")
        .errorCode("PAYMENT_CANCELLED")
        .paymentReference(reference)
        .paymentStatus(PaymentStatus.CANCELLED)
        .timestamp(LocalDateTime.now())
        .build();
    }

    // List  response with pagination
    public static PaymentResponse listSuccess(String message, PaginationData paginationData) {
        return PaymentResponse.builder()
        .success(true)
        .message(message)
        .pagination(paginationData)
        .timestamp(LocalDateTime.now())
        .build();
    }


    public static Object successResponse(String string, PaymentAnalytics analytics) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'successResponse'");
    }
    
    
}
