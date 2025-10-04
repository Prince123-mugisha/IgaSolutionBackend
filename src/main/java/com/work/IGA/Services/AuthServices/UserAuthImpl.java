package com.work.IGA.Services.AuthServices;

import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.BaseSignUpDto;
import com.work.IGA.Utils.InstructorSignUpDto;
import com.work.IGA.Utils.LoginDto;


public interface UserAuthImpl {
    ApiResponse<?> signUpStudent(BaseSignUpDto baseSignUpDto); 
    ApiResponse<?> login(LoginDto loginDTO);
    ApiResponse<?> signUpInstructor(InstructorSignUpDto instructorDto);
} 

