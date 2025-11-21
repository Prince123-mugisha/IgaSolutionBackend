package com.work.IGA.Utils.SubmitionAssignmentUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionDto {
    private UUID id;
    private UUID assignmentId;
    private String assignmentTitle;
    private UUID studentId;
    private String studentName;
    private String submissionFile;
    private String feedback;
    private LocalDateTime submittedAt;
    private Integer grade;
    private LocalDateTime gradedAt;
    private boolean isGraded;
}