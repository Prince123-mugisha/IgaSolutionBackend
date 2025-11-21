package com.work.IGA.Repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.work.IGA.Models.Courses.Enrollment;
import com.work.IGA.Models.Courses.AssignmentsSchema;
import com.work.IGA.Models.Courses.CoursesSchema;
import com.work.IGA.Models.Users.UserSchema;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
   
    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    List<Enrollment> findByStudentIdOrderByEnrollmentDateDesc(UUID studentId);
    Page<Enrollment> findByStudentIdOrderByEnrollmentDateDesc(UUID studentId, Pageable pageable);
    List<Enrollment> findByCourseIdOrderByEnrollmentDateDesc(UUID courseId);
    Page<Enrollment> findByCourseIdOrderByEnrollmentDateDesc(UUID courseId, Pageable pageable);

    long countByCourseId(UUID courseId);
    long countByStudentId(UUID studentId);
    
    // Additional methods for instructor functionality
    @Query("SELECT e FROM Enrollment e WHERE e.course.instructor.id = :instructorId ORDER BY e.enrollmentDate DESC")
    Page<Enrollment> findByCourseInstructorIdOrderByEnrollmentDateDesc(@Param("instructorId") UUID instructorId, Pageable pageable);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.instructor.id = :instructorId")
    List<Enrollment> findByCourseInstructorId(@Param("instructorId") UUID instructorId);
    
    // Entity-based methods
    boolean existsByStudentAndCourse(UserSchema student, CoursesSchema course);
    Page<Enrollment> findByStudentOrderByEnrollmentDateDesc(UserSchema student, Pageable pageable);
    Page<Enrollment> findByCourseOrderByEnrollmentDateDesc(CoursesSchema course, Pageable pageable);
    long countByCourse(CoursesSchema course);
    boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId);

} 
