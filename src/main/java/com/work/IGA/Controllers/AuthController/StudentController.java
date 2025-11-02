package com.work.IGA.Controllers.AuthController;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import com.work.IGA.Services.AuthServices.UserAuth;
import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.BaseSignUpDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserAuth userAuth;
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ApiResponse<?> getStudentProfile(@RequestHeader("Authorization") String token) {
        System.out.println("Received token in StudentController: " + token);
        return userAuth.gettingStudentProfile(token);
    }

   @PutMapping("/update/profile")
   @PreAuthorize("hasAuthority('ROLE_STUDENT')")
   public ApiResponse<?> updateStudentProfile(@RequestBody @Valid BaseSignUpDto baseSignUpDto, @RequestHeader("Authorization") String token) {

       return userAuth.updateStudentProfile(baseSignUpDto, token); 
   }


   @DeleteMapping("/delete/profile")
   @PreAuthorize("hasAuthority('ROLE_STUDENT')")
   public ApiResponse<?> deleteStudentProfile(@RequestHeader("Authorization") String token) {
       return userAuth.deleteStudentProfile(token);
   }

    }
    

