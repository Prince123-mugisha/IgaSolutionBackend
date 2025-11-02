package com.work.IGA.Services.AuthServices;

import java.util.UUID;

import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.BaseSignUpDto;
import com.work.IGA.Utils.InstructorSignUpDto;
import com.work.IGA.Utils.LoginDto;


public interface UserAuthImpl {
    // Authentications Services
    ApiResponse<?> signUpStudent(BaseSignUpDto baseSignUpDto); 
    ApiResponse<?> login(LoginDto loginDTO);
    ApiResponse<?> signUpInstructor(InstructorSignUpDto instructorDto);

    // Student Services
    ApiResponse<?> gettingStudentProfile(String token);
    ApiResponse<?> updateStudentProfile(BaseSignUpDto baseSignUpDto, String token);
    ApiResponse<?> deleteStudentProfile(String token);

    // Admin Services
    ApiResponse<?> gettingAdminProfile(String token);
    ApiResponse<?> ChangingStudentRoletoAdmin(UUID studentId,  String token);
    ApiResponse<?> gettingAllInStructors(String token);
    ApiResponse<?> gettingInstructorDetails(UUID instructorId, String token);
    ApiResponse<?> gettingAllStudents(String token);
    ApiResponse<?> gettingStudentDetails(UUID studentId, String token);
    ApiResponse<?> gettingAllAdmins(String token);
    ApiResponse<?> ApproveInstructor(UUID instructorId, String token);
    ApiResponse<?> RejectInstructor(UUID instructorId, String token);


    // Instructor Services 
    ApiResponse<?> gettingInstructorProfile(String token) ;
    ApiResponse<?> updateInstructorProfile(InstructorSignUpDto instructorDto, String token);
    ApiResponse<?> deleteInstructorProfile(String token);
} 



