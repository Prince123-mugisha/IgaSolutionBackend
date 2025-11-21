package com.work.IGA.Utils.AssignmentUtils;

public class SubmissionStatisticsDto {
    private Long totalSubmissions;
    private Long gradedSubmissions;
    private Long ungradedSubmissions;
    private Long lateSubmissions;
    private Long onTimeSubmissions;
    private Double averageGrade;

    // Constructors
    public SubmissionStatisticsDto() {}

    public SubmissionStatisticsDto(Long totalSubmissions, Long gradedSubmissions, 
                                   Long ungradedSubmissions, Long lateSubmissions, 
                                   Long onTimeSubmissions, Double averageGrade) {
        this.totalSubmissions = totalSubmissions;
        this.gradedSubmissions = gradedSubmissions;
        this.ungradedSubmissions = ungradedSubmissions;
        this.lateSubmissions = lateSubmissions;
        this.onTimeSubmissions = onTimeSubmissions;
        this.averageGrade = averageGrade;
    }

    // Getters and Setters
    public Long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public Long getGradedSubmissions() {
        return gradedSubmissions;
    }

    public void setGradedSubmissions(Long gradedSubmissions) {
        this.gradedSubmissions = gradedSubmissions;
    }

    public Long getUngradedSubmissions() {
        return ungradedSubmissions;
    }

    public void setUngradedSubmissions(Long ungradedSubmissions) {
        this.ungradedSubmissions = ungradedSubmissions;
    }

    public Long getLateSubmissions() {
        return lateSubmissions;
    }

    public void setLateSubmissions(Long lateSubmissions) {
        this.lateSubmissions = lateSubmissions;
    }

    public Long getOnTimeSubmissions() {
        return onTimeSubmissions;
    }

    public void setOnTimeSubmissions(Long onTimeSubmissions) {
        this.onTimeSubmissions = onTimeSubmissions;
    }

    public Double getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(Double averageGrade) {
        this.averageGrade = averageGrade;
    }
}