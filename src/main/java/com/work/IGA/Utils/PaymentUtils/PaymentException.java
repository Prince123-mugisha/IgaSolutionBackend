package com.work.IGA.Utils.PaymentUtils;

import lombok.Getter;

@Getter 
public class PaymentException extends Exception {

    private final String errorCode;
    private final String userMessage;
    private final Object additionalData;

    // Default constructor
    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
        this.userMessage = message;
        this.additionalData = null;
    }

    // Constructor with error code and user message
    public PaymentException(String errorCode, String userMessage) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.additionalData = null;
    }

    // Contructor with error code and user-friendly message 
    public PaymentException(String errorCode, String message , String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.additionalData = null;
    }

    // Contructor with addition data 
    public PaymentException(String errorCode, String message , String userMessage, Object additionalData) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.additionalData = additionalData;
    }

    // COnstructor with couse 
    public PaymentException(String message, Throwable cause) {
        super(message , cause);
        this.errorCode = "PAYMENT_ERROR";
        this.userMessage = message;
        this.additionalData = null;
    }

    // Contructor with error code and cause 
    public PaymentException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.additionalData = null;
    }

    // Static factory method for common payment errors 
    public static PaymentException invalidAmount(double amount) {
        return new PaymentException("INVALID_AMOUNT", 
        "Invalid payment amount: " + amount,
        "The payment amount is invalid. Please check and try again.");
    }

    public static PaymentException userNotFound(String userId) {
        return new PaymentException("USER_NOT_FOUND",
        "User not found with ID :" + userId, 
        "User account not found . Please check your account details");
    }


    public static PaymentException courseNotFound(String courseId) {
        return new PaymentException("COURSE_NOT_FOUND",
        "Course not found with ID :" + courseId, 
        "The selected course was not found . Please check the course details");
    }
    

    public static PaymentException paymentGatewayError(String message) {
        return new PaymentException("PAYMENT_GATEWAY_ERROR",
        "Payment gateway error :" + message, 
        "Payment processing is currently unavailable. Please try again later.");
    }

    public static PaymentException paymentVerificationFailed(String transactionId) {
        return new PaymentException("VERIFICATION_FAILED",
        "Payment verification failed for transaction ID :" + transactionId, 
        "The payment could not be verified. Please contact support.");
    }


    public static PaymentException duplicatedPayment(String reference) {
        return new PaymentException("DUPLICATE_PAYMENT",
        "Duplicate payment detected with reference :" + reference, 
        "A payment with this reference already exists. Please check your payment history.");
    }

    public static PaymentException insufficientFunds() {
        return new PaymentException("INSUFFICIENT_FUNDS",
        "Insufficient funds for the payment", 
        "You have insufficient funds to complete this payment. Please check your account balance.");
    }

   public static PaymentException paymentTimeout() {
        return new PaymentException("PAYMENT_TIMEOUT",
        "Payment process timed out", 
        "The payment process took too long and has timed out. Please try again.");
   }
}