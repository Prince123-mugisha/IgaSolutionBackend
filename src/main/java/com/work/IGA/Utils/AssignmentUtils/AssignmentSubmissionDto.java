package com.work.IGA.Utils.AssignmentUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;
import java.time.LocalDateTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class AssignmentSubmissionDto {
    private UUID submissionId;
    private UUID assignmentId;
    private UUID studentId;
    private String submissionFile;
    private String feedback;
    private LocalDateTime submittedAt;
    private Integer grade;

    public AssignmentSubmissionDto() {}

    public AssignmentSubmissionDto(UUID submissionId, UUID assignmentId, UUID studentId, String submissionFile, String feedback, LocalDateTime submittedAt, Integer grade) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.submissionFile = submissionFile;
        this.feedback = feedback;
        this.submittedAt = submittedAt;
        this.grade = grade;
    }

    public UUID getSubmissionId() { return submissionId; }
    public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }

    public UUID getAssignmentId() { return assignmentId; }
    public void setAssignmentId(UUID assignmentId) { this.assignmentId = assignmentId; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getSubmissionFile() { return submissionFile; }
    public void setSubmissionFile(String submissionFile) { this.submissionFile = submissionFile; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
}
