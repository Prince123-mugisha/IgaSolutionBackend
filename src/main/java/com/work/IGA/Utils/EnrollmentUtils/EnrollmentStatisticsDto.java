package com.work.IGA.Utils.EnrollmentUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentStatisticsDto {
    private long totalEnrollments;
    private long activeEnrollments;
    private long completedEnrollments;
    private long inProgressEnrollments;
    private long droppedEnrollments;
    private double completionRate;
    private String mostPopularCourse;
    private String leastPopularCourse;
    private long totalStudents;
}