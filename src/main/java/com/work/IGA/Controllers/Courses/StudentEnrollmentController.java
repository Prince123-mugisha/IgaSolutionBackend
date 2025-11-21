package com.work.IGA.Controllers.Courses;

import com.work.IGA.Services.CourseServices.EnrollmentService;
import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentDto;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentException;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentResponse;
import com.work.IGA.Models.Courses.ProgressEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/student/enrollments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentEnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Get student's enrolled courses with pagination
     */
    @GetMapping("/my-enrollments")
    public ResponseEntity<ApiResponse<Page<EnrollmentDto>>> getMyEnrolledCourses(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<EnrollmentDto> enrollments = enrollmentService.getStudentEnrollments(authHeader, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Enrolled courses retrieved successfully",
                enrollments,
                null
            ));

        } catch (EnrollmentException e) {
            log.error("Error fetching enrolled courses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching enrolled courses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Update enrollment progress
     */
    @PutMapping("/{enrollmentId}/progress")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateProgress(
            @PathVariable UUID enrollmentId,
            @RequestParam String progress,
            @RequestHeader("Authorization") String authHeader) {
        try {
            ProgressEnum progressEnum = ProgressEnum.valueOf(progress.toUpperCase());
            EnrollmentResponse response = enrollmentService.updateEnrollmentProgress(
                enrollmentId, progressEnum, authHeader);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Progress updated successfully",
                response,
                null
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid progress status"));
        } catch (EnrollmentException e) {
            log.error("Error updating progress: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating progress: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Check if student is enrolled in a specific course
     */
    @GetMapping("/check/{courseId}")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrollmentStatus(
            @PathVariable UUID courseId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(courseId, authHeader);
            
            return ResponseEntity.ok(ApiResponse.success(
                isEnrolled ? "Student is enrolled" : "Student is not enrolled",
                isEnrolled,
                null
            ));

        } catch (Exception e) {
            log.error("Error checking enrollment status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Get enrollment count for a course (public endpoint)
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<ApiResponse<Long>> getCourseEnrollmentCount(
            @PathVariable UUID courseId) {
        try {
            long count = enrollmentService.getCourseEnrollmentCount(courseId);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Enrollment count retrieved successfully",
                count,
                null
            ));

        } catch (Exception e) {
            log.error("Error getting enrollment count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }
}