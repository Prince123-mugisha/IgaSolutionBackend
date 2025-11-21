package com.work.IGA.Repositories;

import com.work.IGA.Models.Courses.AssignmentSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {

  // Find submission by assignment and student
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.student.id = :studentId")
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(@Param("assignmentId") UUID assignmentId, @Param("studentId") UUID studentId);
    
    // Find all submissions by student for a specific course
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.assignment.module.course.id = :courseId")
    List<AssignmentSubmission> findByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
    
    // Find all submissions for an assignment
    List<AssignmentSubmission> findByAssignmentId(UUID assignmentId);
    
    // Find all submissions for a course
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.module.course.id = :courseId")
    List<AssignmentSubmission> findByCourseId(@Param("courseId") UUID courseId);
    
    // Find ungraded submissions for a course
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.module.course.id = :courseId AND s.grades IS NULL")
    List<AssignmentSubmission> findUngradedByCourseId(@Param("courseId") UUID courseId);
    
    // Count total submissions for an assignment
    long countByAssignmentId(UUID assignmentId);
    
    // Count graded submissions for an assignment
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grades IS NOT NULL")
    long countGradedByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    // ...existing code...

@Query(
  value = "SELECT COUNT(*) FROM assignment_submissions s " +
          "JOIN assignments_schema a ON s.assignment_id = a.id " +
          "WHERE s.assignment_id = :assignmentId AND s.submitted_at > a.due_date",
  nativeQuery = true
)
long countLateSubmissionsByAssignmentId(@Param("assignmentId") UUID assignmentId);

// ...existing code...
    
    // Get average grade for an assignment
    @Query("SELECT AVG(g.pointAwarded) FROM Grades g WHERE g.assignmentSubmission.assignment.id = :assignmentId")
    Double getAverageGradeByAssignmentId(@Param("assignmentId") UUID assignmentId);

}