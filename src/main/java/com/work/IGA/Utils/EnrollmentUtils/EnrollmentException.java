package com.work.IGA.Utils.EnrollmentUtils;

import lombok.Getter;

@Getter
public class EnrollmentException extends Exception {
    
    private final String errorCode;

    public EnrollmentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public EnrollmentException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // Common error codes
    public static final String STUDENT_NOT_FOUND = "STUDENT_NOT_FOUND";
    public static final String COURSE_NOT_FOUND = "COURSE_NOT_FOUND";
    public static final String ALREADY_ENROLLED = "ALREADY_ENROLLED";
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
    public static final String PAYMENT_NOT_COMPLETED = "PAYMENT_NOT_COMPLETED";
    public static final String ENROLLMENT_NOT_FOUND = "ENROLLMENT_NOT_FOUND";
    public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
    public static final String ENROLLMENT_CREATION_FAILED = "ENROLLMENT_CREATION_FAILED";
}