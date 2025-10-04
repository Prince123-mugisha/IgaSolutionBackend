package com.work.IGA.Services.AuthServices;

import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.work.IGA.Configuration.CloudinaryService;
import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Configuration.UserDetailsImpl;
import com.work.IGA.Models.Users.Approval;
import com.work.IGA.Models.Users.InstructorData;
import com.work.IGA.Models.Users.Roles;
import com.work.IGA.Models.Users.UserSchema;
import com.work.IGA.Repositories.UserRepository;
import com.work.IGA.Utils.ApiResponse;
import com.work.IGA.Utils.BaseSignUpDto;
import com.work.IGA.Utils.InstructorSignUpDto;
import com.work.IGA.Utils.LoginDto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class UserAuth implements UserAuthImpl {

    private final UserRepository UserRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CloudinaryService cloudinaryService;

    @Override
    public ApiResponse<?> signUpInstructor(InstructorSignUpDto instructorDto) {
        try {
            // Check if email exists
            if (UserRepo.findByEmail(instructorDto.getEmail()).isPresent()) {
                return ApiResponse.error("Email already exists, please  user  another  email  or  login  please ");
            }

            // Upload files to Cloudinary
            String resumeUrl = cloudinaryService.uploadFile(instructorDto.getResume());
            String certificateUrl = cloudinaryService.uploadFile(instructorDto.getCertificate());
            String imageUrl = cloudinaryService.uploadFile(instructorDto.getImage());

            // Create User
            UserSchema newUser = new UserSchema();
            newUser.setFirstName(instructorDto.getFirstName());
            newUser.setLastName(instructorDto.getLastName());
            newUser.setEmail(instructorDto.getEmail());
            newUser.setPassword(passwordEncoder.encode(instructorDto.getPassword()));
            newUser.setRole(Roles.INSTRUCTOR);

            // Save User first
            UserSchema savedUser = UserRepo.save(newUser);

            // Create InstructorData
            InstructorData instructorData = new InstructorData();
            instructorData.setPhoneNumber(instructorDto.getPhoneNumber());
            instructorData.setAreaOfExperience(instructorDto.getAreaOfExperience());
            instructorData.setYearOfExperience(instructorDto.getYearsOfExperience());
            instructorData.setProfessionBio(instructorDto.getProfessionalBio());
            instructorData.setResumeUrl(resumeUrl);
            instructorData.setCertificateUrl(certificateUrl);
            instructorData.setImageUrl(imageUrl);
            instructorData.setUser(savedUser);
            instructorData.setApprovalStatus(Approval.PENDING);

            // Set instructor data to user
            savedUser.setInstructorData(instructorData);

            // Save again to update the relationship
            savedUser = UserRepo.save(savedUser);

            return ApiResponse.success(
                "Instructor registration submitted successfully. Please wait for admin approval.",
                savedUser,
                null
            );

        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ApiResponse.error("Server error: " + e.getMessage());
        }
    }

    // Method to handle student sign-up
    @Override
    public ApiResponse<?> signUpStudent(BaseSignUpDto baseSignUpDto) {

       try {

        // Check if user with email already exists
        if(UserRepo.findByEmail(baseSignUpDto.getEmail()).isPresent()) {
            return ApiResponse.error("Email already exists, please use another one or login");
        }

        // Create new User
        UserSchema newUser = new UserSchema();
        newUser.setFirstName(baseSignUpDto.getFirstName());
        newUser.setLastName(baseSignUpDto.getLastName());
        newUser.setEmail(baseSignUpDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(baseSignUpDto.getPassword()));
        newUser.setRole(Roles.STUDENT);

        // Save User
        UserSchema savedUser = UserRepo.save(newUser);

        // Generate jwt token
        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String jwt = jwtUtils.generateJwToken(userDetails);

        return ApiResponse.success(
            "Student registered successfully",
            savedUser,
            jwt
        );

       }
       catch(Exception e){
          return ApiResponse.error("Server error: " + e.getMessage());
       }
    }


    // Method to  handle  login

    @Override
    public ApiResponse<?> login(LoginDto loginDTO) {
        try {
            
            // First find the user to check approval status for instructor
            Optional<UserSchema> userOpt = UserRepo.findByEmail(loginDTO.getEmail());
            if (!userOpt.isPresent()) {
                return ApiResponse.error("Invalid email or password");
            }

            UserSchema user = userOpt.get();

            // Check if approval status for instructor
            if (user.getRole() == Roles.INSTRUCTOR) {
                if (user.getInstructorData().getApprovalStatus() != Approval.APPROVED) {
                    return ApiResponse.error("Your instructor account is pending ,  please wait for admin approval within  10 hours");
                }
            }

            // Proceed with authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(), loginDTO.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwToken(userDetailsImpl);

            return ApiResponse.success(
                "Login successful", null, jwt);




        } catch (Exception e){
            return ApiResponse.error("Invalid email or password");
        }
    }

}

   



    

