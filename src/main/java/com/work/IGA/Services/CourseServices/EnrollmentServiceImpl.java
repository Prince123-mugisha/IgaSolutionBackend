package com.work.IGA.Services.CourseServices;

import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Models.Courses.CoursesSchema;
import com.work.IGA.Models.Courses.Enrollment;
import com.work.IGA.Models.Courses.ProgressEnum;
import com.work.IGA.Models.Users.UserSchema;
import com.work.IGA.Repositories.CourseRepo;
import com.work.IGA.Repositories.EnrollmentRepository;
import com.work.IGA.Repositories.UserRepository;
import com.work.IGA.Utils.EnrollmentUtils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepo courseRepository;
    private final JwtUtils jwtUtils;

    @Override
    public Page<EnrollmentDto> getStudentEnrollments(String token, Pageable pageable) throws EnrollmentException {
        try {
            // Extract JWT token
            String cleanToken = extractToken(token);
            
            // Get user from token
            String email = jwtUtils.getEmailFromJwtToken(cleanToken);
            UserSchema student = userRepository.findByEmail(email)
                .orElseThrow(() -> new EnrollmentException("Student not found", "STUDENT_NOT_FOUND"));

            // Get enrollments
            Page<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(student.getId(), pageable);
            
            return enrollments.map(EnrollmentDto::new);

        } catch (EnrollmentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching student enrollments: {}", e.getMessage());
            throw new EnrollmentException("Failed to fetch enrollments", "FETCH_ENROLLMENTS_FAILED");
        }
    }

    @Override
    public Page<EnrollmentDto> getInstructorCourseEnrollments(String token, Pageable pageable) throws EnrollmentException {
        try {
            // Extract JWT token
            String cleanToken = extractToken(token);
            
            // Get user from token
            String email = jwtUtils.getEmailFromJwtToken(cleanToken);
            UserSchema instructor = userRepository.findByEmail(email)
                .orElseThrow(() -> new EnrollmentException("Instructor not found", "INSTRUCTOR_NOT_FOUND"));

            // Verify user is an instructor
            if (!instructor.getRole().name().equalsIgnoreCase("INSTRUCTOR")) {
                throw new EnrollmentException("Only instructors can view course enrollments", "INVALID_USER_TYPE");
            }

            // Get enrollments for all courses taught by this instructor
            Page<Enrollment> enrollments = enrollmentRepository.findByCourseInstructorIdOrderByEnrollmentDateDesc(instructor.getId(), pageable);
            
            return enrollments.map(EnrollmentDto::new);

        } catch (EnrollmentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching instructor course enrollments: {}", e.getMessage());
            throw new EnrollmentException("Failed to fetch course enrollments", "FETCH_COURSE_ENROLLMENTS_FAILED");
        }
    }

    @Override
    public Page<EnrollmentDto> getCourseEnrollments(UUID courseId, String token, Pageable pageable) throws EnrollmentException {
        try {
            // Extract JWT token
            String cleanToken = extractToken(token);
            
            // Get user from token
            String email = jwtUtils.getEmailFromJwtToken(cleanToken);
            UserSchema instructor = userRepository.findByEmail(email)
                .orElseThrow(() -> new EnrollmentException("Instructor not found", "INSTRUCTOR_NOT_FOUND"));

            // Get course
            CoursesSchema course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EnrollmentException("Course not found", "COURSE_NOT_FOUND"));

            // Verify instructor owns this course
            if (!course.getInstructor().getId().equals(instructor.getId())) {
                throw new EnrollmentException("Unauthorized to view this course's enrollments", "UNAUTHORIZED_ACCESS");
            }

            // Get enrollments for this specific course
            Page<Enrollment> enrollments = enrollmentRepository.findByCourseIdOrderByEnrollmentDateDesc(courseId, pageable);
            
            return enrollments.map(EnrollmentDto::new);

        } catch (EnrollmentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching course enrollments: {}", e.getMessage());
            throw new EnrollmentException("Failed to fetch course enrollments", "FETCH_COURSE_ENROLLMENTS_FAILED");
        }
    }

    @Override
    public EnrollmentResponse updateEnrollmentProgress(UUID enrollmentId, ProgressEnum progress, String token) throws EnrollmentException {
        try {
            // Extract JWT token
            String cleanToken = extractToken(token);
            
            // Get user from token
            String email = jwtUtils.getEmailFromJwtToken(cleanToken);
            UserSchema student = userRepository.findByEmail(email)
                .orElseThrow(() -> new EnrollmentException("Student not found", "STUDENT_NOT_FOUND"));

            // Get enrollment
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException("Enrollment not found", "ENROLLMENT_NOT_FOUND"));

            // Verify student owns this enrollment
            if (!enrollment.getStudent().getId().equals(student.getId())) {
                throw new EnrollmentException("Unauthorized to update this enrollment", "UNAUTHORIZED_ACCESS");
            }

            // Update progress
            enrollment.setProgress(progress.name());
            // Note: Completion date would need to be added to the Enrollment model if required

            enrollment = enrollmentRepository.save(enrollment);

            log.info("Enrollment {} progress updated to {}", enrollmentId, progress);

            return EnrollmentResponse.success("Progress updated successfully", new EnrollmentDto(enrollment));

        } catch (EnrollmentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating enrollment progress: {}", e.getMessage());
            throw new EnrollmentException("Failed to update progress", "UPDATE_PROGRESS_FAILED");
        }
    }

    @Override
    public EnrollmentStatisticsDto getInstructorEnrollmentStatistics(String token) throws EnrollmentException {
        try {
            // Extract JWT token
            String cleanToken = extractToken(token);
            
            // Get user from token
            String email = jwtUtils.getEmailFromJwtToken(cleanToken);
            UserSchema instructor = userRepository.findByEmail(email)
                .orElseThrow(() -> new EnrollmentException("Instructor not found", "INSTRUCTOR_NOT_FOUND"));

            // Verify user is an instructor
            if (!instructor.getRole().name().equalsIgnoreCase("INSTRUCTOR")) {
                throw new EnrollmentException("Only instructors can view enrollment statistics", "INVALID_USER_TYPE");
            }

            // Calculate statistics
            List<Enrollment> allEnrollments = enrollmentRepository.findByCourseInstructorId(instructor.getId());
            
            long totalEnrollments = allEnrollments.size();
            long activeEnrollments = allEnrollments.stream()
                .mapToLong(e -> e.getProgress().equals(ProgressEnum.IN_PROGRESS.name()) ? 1 : 0)
                .sum();
            long completedEnrollments = allEnrollments.stream()
                .mapToLong(e -> e.getProgress().equals(ProgressEnum.COMPLETED.name()) ? 1 : 0)
                .sum();
            long inProgressEnrollments = activeEnrollments;
            long droppedEnrollments = allEnrollments.stream()
                .mapToLong(e -> e.getProgress().equals("DROPPED") ? 1 : 0)
                .sum();
            
            double completionRate = totalEnrollments > 0 ? 
                (double) completedEnrollments / totalEnrollments * 100 : 0.0;

            // Find most and least popular courses
            Map<String, Long> courseCounts = new HashMap<>();
            allEnrollments.forEach(e -> 
                courseCounts.merge(e.getCourse().getCourseName(), 1L, Long::sum));
            
            String mostPopularCourse = courseCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            String leastPopularCourse = courseCounts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

            long totalStudents = allEnrollments.stream()
                .map(e -> e.getStudent().getId())
                .collect(HashSet::new, (set, id) -> set.add(id), HashSet::addAll)
                .size();

            return new EnrollmentStatisticsDto(
                totalEnrollments,
                activeEnrollments,
                completedEnrollments,
                inProgressEnrollments,
                droppedEnrollments,
                completionRate,
                mostPopularCourse,
                leastPopularCourse,
                totalStudents
            );

        } catch (EnrollmentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching enrollment statistics: {}", e.getMessage());
            throw new EnrollmentException("Failed to fetch statistics", "FETCH_STATISTICS_FAILED");
        }
    }

    @Override
    public boolean isStudentEnrolledInCourse(UUID courseId, String token) {
        try {
            // Extract JWT token
            String cleanToken = extractToken(token);
            
            // Get user from token
            String email = jwtUtils.getEmailFromJwtToken(cleanToken);
            UserSchema student = userRepository.findByEmail(email).orElse(null);
            
            if (student == null) return false;

            CoursesSchema course = courseRepository.findById(courseId).orElse(null);
            if (course == null) return false;

            return enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId());

        } catch (Exception e) {
            log.error("Error checking enrollment status: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getCourseEnrollmentCount(UUID courseId) {
        try {
            CoursesSchema course = courseRepository.findById(courseId).orElse(null);
            if (course == null) return 0;

            return enrollmentRepository.countByCourseId(courseId);

        } catch (Exception e) {
            log.error("Error getting course enrollment count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Extract clean token from Authorization header or direct token
     */
    private String extractToken(String token) throws EnrollmentException {
        if (token == null || token.trim().isEmpty()) {
            throw new EnrollmentException("Token is required", "TOKEN_REQUIRED");
        }

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }

        return token.trim();
    }
}