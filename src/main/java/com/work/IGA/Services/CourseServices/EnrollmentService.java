package com.work.IGA.Services.CourseServices;

import com.work.IGA.Utils.EnrollmentUtils.EnrollmentDto;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentException;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentResponse;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentStatisticsDto;
import com.work.IGA.Models.Courses.ProgressEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface EnrollmentService {

    /**
     * Get student's enrolled courses with pagination
     * @param token - JWT token
     * @param pageable - pagination information
     * @return page of enrollment DTOs
     * @throws EnrollmentException if user not found
     */
    Page<EnrollmentDto> getStudentEnrollments(String token, Pageable pageable) throws EnrollmentException;

    /**
     * Get enrollments for instructor's courses
     * @param token - JWT token (instructor)
     * @param pageable - pagination information
     * @return page of enrollment DTOs
     * @throws EnrollmentException if instructor not found
     */
    Page<EnrollmentDto> getInstructorCourseEnrollments(String token, Pageable pageable) throws EnrollmentException;

    /**
     * Get enrollments for a specific course (instructor only)
     * @param courseId - course ID
     * @param token - JWT token (instructor)
     * @param pageable - pagination information
     * @return page of enrollment DTOs
     * @throws EnrollmentException if unauthorized or course not found
     */
    Page<EnrollmentDto> getCourseEnrollments(UUID courseId, String token, Pageable pageable) throws EnrollmentException;

    /**
     * Update enrollment progress
     * @param enrollmentId - enrollment ID
     * @param progress - new progress status
     * @param token - JWT token (student)
     * @return enrollment response
     * @throws EnrollmentException if unauthorized or enrollment not found
     */
    EnrollmentResponse updateEnrollmentProgress(UUID enrollmentId, ProgressEnum progress, String token) throws EnrollmentException;

    /**
     * Get enrollment statistics for instructor
     * @param token - JWT token (instructor)
     * @return enrollment statistics
     * @throws EnrollmentException if instructor not found
     */
    EnrollmentStatisticsDto getInstructorEnrollmentStatistics(String token) throws EnrollmentException;

    /**
     * Check if student is enrolled in course
     * @param courseId - course ID
     * @param token - JWT token (student)
     * @return true if enrolled, false otherwise
     */
    boolean isStudentEnrolledInCourse(UUID courseId, String token);

    /**
     * Get enrollment count for a course
     * @param courseId - course ID
     * @return enrollment count
     */
    long getCourseEnrollmentCount(UUID courseId);
}