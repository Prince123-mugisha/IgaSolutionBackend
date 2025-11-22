package com.work.IGA.Controllers.Courses;

import com.work.IGA.Configuration.UserDetailsImpl;
import com.work.IGA.Services.CourseServices.GradesServices;
import com.work.IGA.Utils.GradeUtils.GradeDto;
import com.work.IGA.Utils.GradeUtils.GradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradesController {
    
    private final GradesServices gradesServices;

    // ================== STUDENT ENDPOINTS ==================
    
    /**
     * Get student's grades for a specific course
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<GradeResponse<List<GradeDto>>> getGradesForCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        try {
            GradeResponse<List<GradeDto>> response = gradesServices.getGradesForCourse(
                userDetails.getId(), courseId);
            
            return response.isSuccess() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.badRequest().body(response);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                GradeResponse.error("Error retrieving grades: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get student's overall grades for all enrolled courses
     */
    @GetMapping("/overall")
    public ResponseEntity<GradeResponse<Map<UUID, List<GradeDto>>>> getOverallGrades(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        try {
            GradeResponse<Map<UUID, List<GradeDto>>> response = gradesServices.getOverallGrades(
                userDetails.getId());
            
            return response.isSuccess() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.badRequest().body(response);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                GradeResponse.error("Error retrieving overall grades: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get student's grade percentages for all courses
     */
    @GetMapping("/percentages")
    public ResponseEntity<GradeResponse<Map<UUID, Double>>> getOverallGradePercentages(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        try {
            GradeResponse<Map<UUID, Double>> response = gradesServices.getOverallGradePercentages(
                userDetails.getId());
            
            return response.isSuccess() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.badRequest().body(response);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                GradeResponse.error("Error retrieving grade percentages: " + e.getMessage())
            );
        }
    }

    // ================== INSTRUCTOR ENDPOINTS ==================
    
    /**
     * Get all grades for a specific course (instructor only)
     */
    @GetMapping("/instructor/course/{courseId}")
    public ResponseEntity<GradeResponse<List<GradeDto>>> getAllGradesForCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        try {
            GradeResponse<List<GradeDto>> response = gradesServices.getAllGradesForCourse(
                userDetails.getId(), courseId);
            
            return response.isSuccess() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.badRequest().body(response);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                GradeResponse.error("Error retrieving course grades: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get grade summary/statistics for instructor's course
     */
    @GetMapping("/instructor/course/{courseId}/summary")
    public ResponseEntity<GradeResponse<Map<String, Object>>> getGradeSummary(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        try {
            // Get all grades for the course
            GradeResponse<List<GradeDto>> gradesResponse = gradesServices.getAllGradesForCourse(
                userDetails.getId(), courseId);
            
            if (!gradesResponse.isSuccess()) {
                return ResponseEntity.badRequest().body(
                    GradeResponse.error(gradesResponse.getMessage())
                );
            }
            
            List<GradeDto> grades = gradesResponse.getData();
            
            // Calculate summary statistics
            Map<String, Object> summary = calculateGradeSummary(grades);
            
            return ResponseEntity.ok(
                GradeResponse.success("Grade summary retrieved successfully", summary)
            );
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                GradeResponse.error("Error retrieving grade summary: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get grades for a specific student in instructor's course
     */
    @GetMapping("/instructor/student/{studentId}/course/{courseId}")
    public ResponseEntity<GradeResponse<List<GradeDto>>> getStudentGradesInCourse(
            @PathVariable UUID studentId,
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        try {
            // First verify instructor owns the course by trying to get all grades
            GradeResponse<List<GradeDto>> allGradesResponse = gradesServices.getAllGradesForCourse(
                userDetails.getId(), courseId);
            
            if (!allGradesResponse.isSuccess()) {
                return ResponseEntity.badRequest().body(
                    GradeResponse.error("Unauthorized or course not found")
                );
            }
            
            // Get specific student's grades for this course
            GradeResponse<List<GradeDto>> response = gradesServices.getGradesForCourse(
                studentId, courseId);
            
            return response.isSuccess() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.badRequest().body(response);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                GradeResponse.error("Error retrieving student grades: " + e.getMessage())
            );
        }
    }
    
    // ================== HELPER METHODS ==================
    
    /**
     * Calculate grade summary statistics
     */
    private Map<String, Object> calculateGradeSummary(List<GradeDto> grades) {
        Map<String, Object> summary = new java.util.HashMap<>();
        
        if (grades == null || grades.isEmpty()) {
            summary.put("totalGrades", 0);
            summary.put("averageGrade", 0.0);
            summary.put("highestGrade", 0);
            summary.put("lowestGrade", 0);
            summary.put("passingRate", 0.0);
            return summary;
        }
        
        int totalGrades = grades.size();
        int totalPoints = grades.stream().mapToInt(GradeDto::getPointsAwarded).sum();
        double averageGrade = totalGrades > 0 ? (double) totalPoints / totalGrades : 0.0;
        
        int highestGrade = grades.stream().mapToInt(GradeDto::getPointsAwarded).max().orElse(0);
        int lowestGrade = grades.stream().mapToInt(GradeDto::getPointsAwarded).min().orElse(0);
        
        // Calculate passing rate (assuming 60% is passing)
        long passingGrades = grades.stream().filter(g -> g.getPointsAwarded() >= 60).count();
        double passingRate = totalGrades > 0 ? (double) passingGrades / totalGrades * 100 : 0.0;
        
        // Grade distribution
        long aGrades = grades.stream().filter(g -> g.getPointsAwarded() >= 90).count();
        long bGrades = grades.stream().filter(g -> g.getPointsAwarded() >= 80 && g.getPointsAwarded() < 90).count();
        long cGrades = grades.stream().filter(g -> g.getPointsAwarded() >= 70 && g.getPointsAwarded() < 80).count();
        long dGrades = grades.stream().filter(g -> g.getPointsAwarded() >= 60 && g.getPointsAwarded() < 70).count();
        long fGrades = grades.stream().filter(g -> g.getPointsAwarded() < 60).count();
        
        summary.put("totalGrades", totalGrades);
        summary.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);
        summary.put("highestGrade", highestGrade);
        summary.put("lowestGrade", lowestGrade);
        summary.put("passingRate", Math.round(passingRate * 100.0) / 100.0);
        
        Map<String, Long> gradeDistribution = new java.util.HashMap<>();
        gradeDistribution.put("A", aGrades);
        gradeDistribution.put("B", bGrades);
        gradeDistribution.put("C", cGrades);
        gradeDistribution.put("D", dGrades);
        gradeDistribution.put("F", fGrades);
        
        summary.put("gradeDistribution", gradeDistribution);
        
        return summary;
    }
}