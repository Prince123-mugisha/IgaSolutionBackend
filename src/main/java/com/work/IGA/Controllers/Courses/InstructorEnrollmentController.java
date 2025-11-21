package com.work.IGA.Controllers.Courses;

import com.work.IGA.Services.CourseServices.EnrollmentService;
import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentDto;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentException;
import com.work.IGA.Utils.EnrollmentUtils.EnrollmentStatisticsDto;

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
@RequestMapping("/api/instructor/enrollments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InstructorEnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Get all enrollments for instructor's courses with pagination
     */
    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponse<Page<EnrollmentDto>>> getMyCourseEnrollments(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<EnrollmentDto> enrollments = enrollmentService.getInstructorCourseEnrollments(authHeader, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Course enrollments retrieved successfully",
                enrollments,
                null
            ));

        } catch (EnrollmentException e) {
            log.error("Error fetching instructor course enrollments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching instructor course enrollments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Get enrollments for a specific course (instructor must own the course)
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<Page<EnrollmentDto>>> getCourseEnrollments(
            @PathVariable UUID courseId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<EnrollmentDto> enrollments = enrollmentService.getCourseEnrollments(courseId, authHeader, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Course enrollments retrieved successfully",
                enrollments,
                null
            ));

        } catch (EnrollmentException e) {
            log.error("Error fetching course enrollments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching course enrollments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Get enrollment statistics for instructor
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<EnrollmentStatisticsDto>> getEnrollmentStatistics(
            @RequestHeader("Authorization") String authHeader) {
        try {
            EnrollmentStatisticsDto statistics = enrollmentService.getInstructorEnrollmentStatistics(authHeader);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Enrollment statistics retrieved successfully",
                statistics,
                null
            ));

        } catch (EnrollmentException e) {
            log.error("Error fetching enrollment statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching enrollment statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Get enrollment count for a specific course
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<ApiResponse<Long>> getCourseEnrollmentCount(
            @PathVariable UUID courseId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // First verify the instructor owns this course by getting course enrollments
            // This will throw an exception if unauthorized
            enrollmentService.getCourseEnrollments(courseId, authHeader, PageRequest.of(0, 1));
            
            long count = enrollmentService.getCourseEnrollmentCount(courseId);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Enrollment count retrieved successfully",
                count,
                null
            ));

        } catch (EnrollmentException e) {
            log.error("Error getting course enrollment count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting enrollment count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Get detailed enrollment information for a specific student in instructor's course
     */
    @GetMapping("/course/{courseId}/student/{studentId}")
    public ResponseEntity<ApiResponse<EnrollmentDto>> getStudentEnrollmentDetails(
            @PathVariable UUID courseId,
            @PathVariable UUID studentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Get all enrollments for the course to find the specific student
            Page<EnrollmentDto> courseEnrollments = enrollmentService.getCourseEnrollments(
                courseId, authHeader, PageRequest.of(0, 100));
            
            // Find the enrollment for the specific student
            EnrollmentDto studentEnrollment = courseEnrollments.getContent().stream()
                .filter(enrollment -> enrollment.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new EnrollmentException("Student enrollment not found", "ENROLLMENT_NOT_FOUND"));
            
            return ResponseEntity.ok(ApiResponse.success(
                "Student enrollment details retrieved successfully",
                studentEnrollment,
                null
            ));

        } catch (EnrollmentException e) {
            log.error("Error fetching student enrollment details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching student enrollment details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
        }
    }
}