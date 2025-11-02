package com.work.IGA.Controllers.AuthController;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.work.IGA.Services.AuthServices.UserAuth;
import com.work.IGA.Utils.ApiResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserAuth userAuth;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getAdminProfile(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.gettingAdminProfile(token));
    }

    @PutMapping("change-role/{studentId}")
    public ResponseEntity<ApiResponse<?>> changeStudentToAdmin(
        @PathVariable UUID studentId,
        @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(userAuth.ChangingStudentRoletoAdmin(studentId, token));
    }

    @GetMapping("/instructors")
    public ResponseEntity<ApiResponse<?>> getAllInstructors(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.gettingAllInStructors(token));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<?>> getInstructorDetails(
            @PathVariable UUID instructorId,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.gettingInstructorDetails(instructorId, token));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<?>> getAllStudents(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.gettingAllStudents(token));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<?>> getStudentDetails(
            @PathVariable UUID studentId,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.gettingStudentDetails(studentId, token));
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<?>> getAllAdmins(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.gettingAllAdmins(token));
    }

    @PutMapping("/instructor/approve/{instructorId}")
    public ResponseEntity<ApiResponse<?>> approveInstructor(
            @PathVariable UUID instructorId,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.ApproveInstructor(instructorId, token));
    }

    @DeleteMapping("/instructor/reject/{instructorId}")
    public ResponseEntity<ApiResponse<?>> rejectInstructor(
            @PathVariable UUID instructorId,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userAuth.RejectInstructor(instructorId, token));
    }
}
