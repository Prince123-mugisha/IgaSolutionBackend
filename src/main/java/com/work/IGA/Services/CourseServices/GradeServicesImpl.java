package com.work.IGA.Services.CourseServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.work.IGA.Models.Courses.CoursesSchema;
import com.work.IGA.Models.Courses.Enrollment;
import com.work.IGA.Models.Courses.Grades;
import com.work.IGA.Repositories.CourseRepo;
import com.work.IGA.Repositories.GradesRepository;
import com.work.IGA.Utils.GradeUtils.GradeDto;
import com.work.IGA.Utils.GradeUtils.GradeException;
import com.work.IGA.Utils.GradeUtils.GradeResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradeServicesImpl implements GradesServices {

    private final GradesRepository gradesRepository;
    private final EnrollmentService enrollmentService;
    private final CourseRepo courseRepo;
    private final com.work.IGA.Repositories.EnrollmentRepository enrollmentRepository;


     @Override
    public GradeResponse<List<GradeDto>> getGradesForCourse(UUID studentId, UUID courseId) {
        try {
            List<Grades> grades = gradesRepository.findByStudentIdAndCourseId(studentId, courseId);
            List<GradeDto> dtos = grades.stream().map(g -> new GradeDto(
                g.getId(),
                g.getAssignmentSubmission().getAssignment().getId(),
                g.getEnrollment().getStudent().getId(),
                g.getPointAwarded(),
                g.getAssignmentSubmission().getFeedback(),
                g.getGradedAt()
            )).collect(Collectors.toList());
            return GradeResponse.success("Grades retrieved", dtos);
        } catch (Exception e) {
            return GradeResponse.error("Error retrieving grades: " + e.getMessage());
        }
    }

    @Override
    public GradeResponse<Map<UUID, List<GradeDto>>> getOverallGrades(UUID studentId) {
        try {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(studentId);
            Map<UUID, List<GradeDto>> courseGrades = new HashMap<>();
            for (Enrollment enrollment : enrollments) {
                UUID courseId = enrollment.getCourse().getId();
                List<Grades> grades = gradesRepository.findByStudentIdAndCourseId(studentId, courseId);
                List<GradeDto> dtos = grades.stream().map(g -> new GradeDto(
                    g.getId(),
                    g.getAssignmentSubmission().getAssignment().getId(),
                    g.getEnrollment().getStudent().getId(),
                    g.getPointAwarded(),
                    g.getAssignmentSubmission().getFeedback(),
                    g.getGradedAt()
                )).collect(Collectors.toList());
                courseGrades.put(courseId, dtos);
            }
            return GradeResponse.success("Overall grades retrieved", courseGrades);
        } catch (Exception e) {
            return GradeResponse.error("Error retrieving overall grades: " + e.getMessage());
        }
    }

    @Override
    public GradeResponse<List<GradeDto>> getAllGradesForCourse(UUID instructorId, UUID courseId) {
        try {
            CoursesSchema course = courseRepo.findById(courseId)
                .orElseThrow(() -> new GradeException("Course not found"));
            if (!course.getInstructor().getId().equals(instructorId)) {
                throw new GradeException("Unauthorized: Not the instructor of this course");
            }
            List<Enrollment> enrollments = enrollmentRepository.findByCourseIdOrderByEnrollmentDateDesc(courseId);
            List<GradeDto> allGrades = new ArrayList<>();
            for (Enrollment enrollment : enrollments) {
                List<Grades> grades = gradesRepository.findByEnrollmentId(enrollment.getId());
                allGrades.addAll(grades.stream().map(g -> new GradeDto(
                    g.getId(),
                    g.getAssignmentSubmission().getAssignment().getId(),
                    g.getEnrollment().getStudent().getId(),
                    g.getPointAwarded(),
                    g.getAssignmentSubmission().getFeedback(),
                    g.getGradedAt()
                )).collect(Collectors.toList()));
            }
            return GradeResponse.success("All grades for course retrieved", allGrades);
        } catch (Exception e) {
            return GradeResponse.error("Error retrieving grades for course: " + e.getMessage());
        }
    }

    @Override
    public GradeResponse<Map<UUID, Double>> getOverallGradePercentages(UUID studentId) {
        try {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(studentId);
            Map<UUID, Double> coursePercentages = new HashMap<>();
            for (Enrollment enrollment : enrollments) {
                UUID courseId = enrollment.getCourse().getId();
                List<Grades> grades = gradesRepository.findByStudentIdAndCourseId(studentId, courseId);
                int totalEarned = grades.stream().mapToInt(Grades::getPointAwarded).sum();
                int totalPossible = grades.stream()
                    .mapToInt(g -> g.getAssignmentSubmission().getAssignment().getMaxPoints())
                    .sum();
                double percentage = totalPossible > 0 ? (totalEarned * 100.0) / totalPossible : 0.0;
                coursePercentages.put(courseId, percentage);
            }
            return GradeResponse.success("Overall grade percentages retrieved", coursePercentages);
        } catch (Exception e) {
            return GradeResponse.error("Error retrieving grade percentages: " + e.getMessage());
        }}
    
}
