package com.work.IGA.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.work.IGA.Models.Courses.Grades;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradesRepository extends JpaRepository<Grades, UUID> {

    // Find grades by enrollment
    List<Grades> findByEnrollmentId(UUID enrollmentId);
    
    // Find grades by student and course
    @Query("SELECT g FROM Grades g WHERE g.enrollment.student.id = :studentId AND g.enrollment.course.id = :courseId")
    List<Grades> findByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
    
    // Find grade by assignment submission
    @Query("SELECT g FROM Grades g WHERE g.assignmentSubmission.id = :submissionId")
    Grades findByAssignmentSubmissionId(@Param("submissionId") UUID submissionId);
}