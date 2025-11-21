
package com.work.IGA.Controllers.Courses;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.work.IGA.Services.CourseServices.AssignmentServices;
import com.work.IGA.Utils.AssignmentUtils.AssignmentDto;
import com.work.IGA.Utils.AssignmentUtils.AssignmentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assignment")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentServices assignmentService;

    @PostMapping("/create")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @ModelAttribute AssignmentDto assignmentDto,
            @RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        token = token.startsWith("Bearer ") ? token.substring(7) : token;
        AssignmentResponse response = assignmentService.createAssignment(assignmentDto, token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<AssignmentResponse> getAllAssignments() {
        AssignmentResponse response = assignmentService.getAllAssignments();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<AssignmentResponse> getAssignmentsByModuleId(
            @PathVariable UUID moduleId) {
        AssignmentResponse response = assignmentService.getAssignmentsByModuleId(moduleId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            @PathVariable UUID assignmentId) {
        AssignmentResponse response = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update/{assignmentId}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable UUID assignmentId,
            @ModelAttribute AssignmentDto assignmentDto,
            @RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        token = token.startsWith("Bearer ") ? token.substring(7) : token;
        AssignmentResponse response = assignmentService.updateAssignment(assignmentId, assignmentDto, token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete/{assignmentId}")
    public ResponseEntity<AssignmentResponse> deleteAssignment(
            @PathVariable UUID assignmentId,
            @RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        token = token.startsWith("Bearer ") ? token.substring(7) : token;
        AssignmentResponse response = assignmentService.deleteAssignment(assignmentId, token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}