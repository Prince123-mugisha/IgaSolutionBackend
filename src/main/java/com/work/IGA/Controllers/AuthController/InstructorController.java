package com.work.IGA.Controllers.AuthController;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.work.IGA.Services.AuthServices.UserAuth;
import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.InstructorSignUpDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorController {

    private final UserAuth userAuth;

    // Controller for getting instructor profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(
            @RequestHeader("Authorization") String token
    ) {
        ApiResponse<?> response = userAuth.gettingInstructorProfile(token);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Controller for updating instructor profile
    @PutMapping("/profile/update")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @Valid @ModelAttribute InstructorSignUpDto instructorDto,
            @RequestHeader("Authorization") String token
    ) {
         ApiResponse<?> response = userAuth.updateInstructorProfile(instructorDto, token);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    // Controller for  deleting instructor profile
    @DeleteMapping("/profile/delete")
    public ResponseEntity<ApiResponse<?>> deleteProfile(
            @RequestHeader("Authorization") String token
    ) {
        ApiResponse<?> response = userAuth.deleteInstructorProfile(token);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
    
}
