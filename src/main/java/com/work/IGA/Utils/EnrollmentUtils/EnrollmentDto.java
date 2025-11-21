package com.work.IGA.Utils.EnrollmentUtils;

import com.work.IGA.Models.Courses.Enrollment;
import com.work.IGA.Models.Courses.ProgressEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto {
    
    private UUID id;
    private UUID courseId;
    private String courseName;
    private String courseDescription;
    private String instructorName;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private LocalDateTime enrollmentDate;
    private ProgressEnum progress;
    private double coursePrice;
    private String courseImageUrl;
    private int courseDurationInHours;

    // Constructor from Enrollment entity
    public EnrollmentDto(Enrollment enrollment) {
        this.id = enrollment.getId();
        this.enrollmentDate = enrollment.getEnrollmentDate();
        this.progress = ProgressEnum.valueOf(enrollment.getProgress());
        
        if (enrollment.getCourse() != null) {
            this.courseId = enrollment.getCourse().getId();
            this.courseName = enrollment.getCourse().getCourseName();
            this.courseDescription = enrollment.getCourse().getCourseDescription();
            this.coursePrice = enrollment.getCourse().getPrice();
            this.courseImageUrl = enrollment.getCourse().getImageUrl();
            this.courseDurationInHours = enrollment.getCourse().getDurationInHours();
            
            if (enrollment.getCourse().getInstructor() != null) {
                this.instructorName = enrollment.getCourse().getInstructor().getFirstName() + 
                                   " " + enrollment.getCourse().getInstructor().getLastName();
            }
        }
        
        if (enrollment.getStudent() != null) {
            this.studentId = enrollment.getStudent().getId();
            this.studentName = enrollment.getStudent().getFirstName() + 
                             " " + enrollment.getStudent().getLastName();
            this.studentEmail = enrollment.getStudent().getEmail();
        }
    }
}