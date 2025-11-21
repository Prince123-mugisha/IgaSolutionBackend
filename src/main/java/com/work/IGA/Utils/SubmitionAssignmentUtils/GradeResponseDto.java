package com.work.IGA.Utils.SubmitionAssignmentUtils;

import java.util.UUID;
import java.time.LocalDateTime;

public class GradeResponseDto {
    private UUID gradeId;
    private UUID assignmentId;
    private UUID studentId;
    private int pointsAwarded;
    private String feedback;
    private LocalDateTime gradedAt;

    public GradeResponseDto() {}

    public GradeResponseDto(UUID gradeId, UUID assignmentId, UUID studentId, int pointsAwarded, String feedback, LocalDateTime gradedAt) {
        this.gradeId = gradeId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.pointsAwarded = pointsAwarded;
        this.feedback = feedback;
        this.gradedAt = gradedAt;
    }

    public UUID getGradeId() { return gradeId; }
    public void setGradeId(UUID gradeId) { this.gradeId = gradeId; }

    public UUID getAssignmentId() { return assignmentId; }
    public void setAssignmentId(UUID assignmentId) { this.assignmentId = assignmentId; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public int getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(int pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
