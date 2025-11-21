package com.work.IGA.Controllers.Courses;

import com.work.IGA.Configuration.UserDetailsImpl;
import com.work.IGA.Models.Courses.*;
import com.work.IGA.Services.CourseServices.SubmitAssignmentService;
import com.work.IGA.Utils.AssignmentUtils.*;
import com.work.IGA.Utils.SubmitionAssignmentUtils.GradeRequest;
import com.work.IGA.Utils.SubmitionAssignmentUtils.SubmissionResponse;
import com.work.IGA.Utils.SubmitionAssignmentUtils.SubmissionDto;
import com.work.IGA.Utils.SubmitionAssignmentUtils.GradeResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    
    private final SubmitAssignmentService submitAssignmentService;

    // ================== STUDENT ENDPOINTS ==================
    // User info comes from JWT token (@AuthenticationPrincipal UserDetailsImpl userDetails)
    
    @PostMapping("/submit")
    public ResponseEntity<SubmissionResponse<SubmissionDto>> submitAssignment(
            @RequestParam("assignmentId") UUID assignmentId,
            @RequestParam("submissionFile") MultipartFile submissionFile,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
                SubmissionRequest request = new SubmissionRequest();
                request.setAssignmentId(assignmentId);
                request.setSubmissionFile(submissionFile);
    
                SubmissionResponse<SubmissionDto> response = submitAssignmentService.submitAssignment(request, userDetails.getId());
                return response.isSuccess() ?
                    ResponseEntity.ok(response) :
                    ResponseEntity.badRequest().body(response);
            }
    
    @PutMapping("/update/{submissionId}")
    public ResponseEntity<SubmissionResponse<SubmissionDto>> updateSubmission(
            @PathVariable UUID submissionId,
            @RequestParam("submissionFile") MultipartFile submissionFile,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
        // userDetails.getId() extracts student ID from JWT token
        SubmissionResponse<SubmissionDto> response = submitAssignmentService
            .updateSubmission(submissionId, submissionFile, userDetails.getId());
        
        return response.isSuccess() ? 
            ResponseEntity.ok(response) : 
            ResponseEntity.badRequest().body(response);
    }
    
    @DeleteMapping("/delete/{submissionId}")
    public ResponseEntity<SubmissionResponse<String>> deleteSubmission(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
        // userDetails.getId() extracts student ID from JWT token
        SubmissionResponse<String> response = submitAssignmentService
            .deleteSubmission(submissionId, userDetails.getId());
        
        return response.isSuccess() ? 
            ResponseEntity.ok(response) : 
            ResponseEntity.badRequest().body(response);
    }
    
    @GetMapping("/my-submissions/{courseId}")
    public ResponseEntity<SubmissionResponse<List<SubmissionDto>>> getMySubmissions(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
        // userDetails.getId() extracts student ID from JWT token
        SubmissionResponse<List<SubmissionDto>> response = submitAssignmentService
            .getStudentSubmissions(userDetails.getId(), courseId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-submission/{submissionId}")
    public ResponseEntity<SubmissionResponse<SubmissionDto>> getMySubmission(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
        // userDetails.getId() extracts student ID from JWT token
        SubmissionResponse<SubmissionDto> response = submitAssignmentService
            .getSubmissionById(submissionId, userDetails.getId());
        
        return response.isSuccess() ? 
            ResponseEntity.ok(response) : 
            ResponseEntity.badRequest().body(response);
    }
    
    @GetMapping("/available-assignments/{courseId}")
    public ResponseEntity<SubmissionResponse<List<AssignmentsSchema>>> getAvailableAssignments(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
        // userDetails.getId() extracts student ID from JWT token
        SubmissionResponse<List<AssignmentsSchema>> response = submitAssignmentService
            .getAvailableAssignments(userDetails.getId(), courseId);
        
        return ResponseEntity.ok(response);
    }

    // ================== INSTRUCTOR ENDPOINTS ==================
    // User info comes from JWT token (@AuthenticationPrincipal UserDetailsImpl userDetails)
    
    @PostMapping("/grade")
    public ResponseEntity<SubmissionResponse<com.work.IGA.Utils.SubmitionAssignmentUtils.GradeResponseDto>> gradeSubmission(
            @Valid @RequestBody GradeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SubmissionResponse<Grades> response = submitAssignmentService.gradeSubmission(request, userDetails.getId());
        GradeResponseDto dto = null;
        if (response.getData() != null) {
            Grades grade = response.getData();
            dto = new GradeResponseDto(
                grade.getId(),
                grade.getAssignmentSubmission().getAssignment().getId(),
                grade.getAssignmentSubmission().getStudent().getId(),
                grade.getPointAwarded(),
                grade.getAssignmentSubmission().getFeedback(),
                grade.getGradedAt()
            );
        }
        SubmissionResponse<GradeResponseDto> dtoResponse = new SubmissionResponse<>(
            response.isSuccess(),
            response.getMessage(),
            dto
        );
        return dtoResponse.isSuccess() ?
            ResponseEntity.ok(dtoResponse) :
            ResponseEntity.badRequest().body(dtoResponse);
    }
    
    @PutMapping("/grade/{gradeId}")
    public ResponseEntity<SubmissionResponse<com.work.IGA.Utils.SubmitionAssignmentUtils.GradeResponseDto>> updateGrade(
            @PathVariable UUID gradeId,
            @Valid @RequestBody GradeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SubmissionResponse<Grades> response = submitAssignmentService.updateGrade(gradeId, request, userDetails.getId());
        GradeResponseDto dto = null;
        if (response.getData() != null) {
            Grades grade = response.getData();
            dto = new GradeResponseDto(
                grade.getId(),
                grade.getAssignmentSubmission().getAssignment().getId(),
                grade.getAssignmentSubmission().getStudent().getId(),
                grade.getPointAwarded(),
                grade.getAssignmentSubmission().getFeedback(),
                grade.getGradedAt()
            );
        }
        SubmissionResponse<GradeResponseDto> dtoResponse = new SubmissionResponse<>(
            response.isSuccess(),
            response.getMessage(),
            dto
        );
        return dtoResponse.isSuccess() ?
            ResponseEntity.ok(dtoResponse) :
            ResponseEntity.badRequest().body(dtoResponse);
    }
    
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<SubmissionResponse<List<SubmissionDto>>> getSubmissionsForAssignment(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SubmissionResponse<List<SubmissionDto>> response = submitAssignmentService.getSubmissionsForAssignment(assignmentId, userDetails.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<SubmissionResponse<List<SubmissionDto>>> getSubmissionsForCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SubmissionResponse<List<SubmissionDto>> response = submitAssignmentService.getSubmissionsForCourse(courseId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/{assignmentId}")
    public ResponseEntity<SubmissionResponse<SubmissionStatisticsDto>> getSubmissionStatistics(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
        // userDetails.getId() extracts instructor ID from JWT token
        SubmissionResponse<SubmissionStatisticsDto> response = submitAssignmentService
            .getSubmissionStatics(assignmentId, userDetails.getId());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ungraded/{courseId}")
    public ResponseEntity<SubmissionResponse<List<SubmissionDto>>> getUngradedSubmissions(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // JWT token provides user info
        
        // userDetails.getId() extracts instructor ID from JWT token
        SubmissionResponse<List<SubmissionDto>> response = submitAssignmentService
            .getUngradedSubmission(courseId, userDetails.getId());
        
        return ResponseEntity.ok(response);
    }
}
