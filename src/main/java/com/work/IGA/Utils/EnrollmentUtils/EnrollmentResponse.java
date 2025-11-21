package com.work.IGA.Utils.EnrollmentUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    
    private boolean success;
    private String message;
    private EnrollmentDto data;
    private String errorCode;

    public static EnrollmentResponse success(String message, EnrollmentDto data) {
        return new EnrollmentResponse(true, message, data, null);
    }

    public static EnrollmentResponse error(String message, String errorCode) {
        return new EnrollmentResponse(false, message, null, errorCode);
    }

    public static EnrollmentResponse fromException(EnrollmentException e) {
        return new EnrollmentResponse(false, e.getMessage(), null, e.getErrorCode());
    }
}