package com.work.IGA.Utils.AssignmentUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradingRequest {
    private UUID submissionId;
    private int grade; // Grade out of maxPoints
    private String feedback; // Instructor feedback
}