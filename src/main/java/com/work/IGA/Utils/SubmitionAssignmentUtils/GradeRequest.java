package com.work.IGA.Utils.SubmitionAssignmentUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeRequest {
    private UUID submissionId;
    
    @NotNull(message = "Points awarded cannot be null")
    @Min(value = 0, message = "Points awarded cannot be negative")
    @Max(value = 100, message = "Points awarded cannot exceed 100")
    private Integer pointsAwarded;
    
    @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
    private String feedback;
}