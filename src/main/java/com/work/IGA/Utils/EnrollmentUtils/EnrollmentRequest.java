package com.work.IGA.Utils.EnrollmentUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {
    
    private UUID courseId;
    private String token; // JWT token will be set by controller
    
    // Constructor without token (token will be added by controller)
    public EnrollmentRequest(UUID courseId) {
        this.courseId = courseId;
    }
}