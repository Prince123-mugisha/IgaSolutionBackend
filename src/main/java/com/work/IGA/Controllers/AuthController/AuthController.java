package com.work.IGA.Controllers.AuthController;

import com.work.IGA.Services.AuthServices.UserAuth;
import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.BaseSignUpDto;
import com.work.IGA.Utils.InstructorSignUpDto;
import com.work.IGA.Utils.LoginDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
   
private final UserAuth userAuth;


    // Controller for SignUp Student
    @PostMapping("/signup/student")
    public ResponseEntity<ApiResponse<?>> SignUpStudent(@Valid @RequestBody BaseSignUpDto signUpDto){
        ApiResponse<?> response = userAuth.signUpStudent(signUpDto);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(409).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginDto loginDto){
        ApiResponse<?> response = userAuth.login(loginDto);
        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.status(409).body(response);
        }
    } 
    
    
    // Controller  for  SignUp  Instructor
    @PostMapping("/signup/instructor")
    public ResponseEntity<ApiResponse<?>> signUpInstructor(
            @Valid @ModelAttribute InstructorSignUpDto instructorSignUpDto
    ) {
        ApiResponse<?> response = userAuth.signUpInstructor(instructorSignUpDto);
        if (response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        else {
            return ResponseEntity.status(409).body(response);
        }
    }   

}