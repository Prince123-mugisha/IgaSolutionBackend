package com.work.IGA.Utils.SubmitionAssignmentUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionStatisticsDto {
    private long totalSubmissions;
    private long gradedSubmissions;
    private long ungradedSubmissions;
    private long lateSubmissions;
    private Double averageGrade;
}