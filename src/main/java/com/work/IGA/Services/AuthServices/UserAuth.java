package com.work.IGA.Services.AuthServices;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Configuration.SupabaseStorageService;
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
    private final SupabaseStorageService supabaseStorageService;
 
   // Method to handle instructor sign-up
   @Override
   public ApiResponse<?> signUpInstructor(InstructorSignUpDto instructorDto) {
     try {
        // Check if user with email already exists 
        if(UserRepo.findByEmail(instructorDto.getEmail()).isPresent()) {
            return ApiResponse.error("Email already exists, please use another one or login");
        }

        // Create new User 
        UserSchema newInstructor = new UserSchema();
        newInstructor.setFirstName(instructorDto.getFirstName());
        newInstructor.setLastName(instructorDto.getLastName());
        newInstructor.setEmail(instructorDto.getEmail());
        newInstructor.setPassword(passwordEncoder.encode(instructorDto.getPassword()));
        newInstructor.setRole(Roles.INSTRUCTOR);


        // Create InstructorData 
        InstructorData instructorData = new InstructorData();
        instructorData.setPhoneNumber(instructorDto.getPhoneNumber());
        instructorData.setAreaOfExperience(instructorDto.getAreaOfExperience());
        instructorData.setYearOfExperience(instructorDto.getYearsOfExperience());
        instructorData.setProfessionBio(instructorDto.getProfessionalBio());
        instructorData.setUser(newInstructor);
        instructorData.setApprovalStatus(Approval.PENDING);
    
         try {
             String resumeUrl = supabaseStorageService.uploadToInstructorFile(
                instructorDto.getResume(),
                "resumes"
             );
             instructorData.setResumeUrl(resumeUrl);

             String certificateUrl = supabaseStorageService.uploadToInstructorFile(
                instructorDto.getCertificate(),
                "certificates"
             );

             instructorData.setCertificateUrl(certificateUrl);

             String imageUrl = supabaseStorageService.uploadToInstructorFile(
                instructorDto.getImage(),
                "profile-images"
             );
             instructorData.setImageUrl(imageUrl);
         }
         catch (Exception e ) {
            return ApiResponse.error("Error to upload files: " + e.getMessage());
         }

         newInstructor.setInstructorData(instructorData);

         // Save User to database
         UserSchema savedInstructor = UserRepo.save(newInstructor);

         // Generate jwt token 
         UserDetailsImpl userDetails = UserDetailsImpl.build(savedInstructor);
         String jwt = jwtUtils.generateJwToken(userDetails);

         return ApiResponse.success(
            "Instructor registered successfully, please wait for admin approval",
            savedInstructor,
            jwt
         );
     }
     catch(Exception e) {
        return ApiResponse.error("Server error:" + e.getMessage());
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
        System.out.println("Saved user with role: " + savedUser.getRole());

        // Generate jwt token
        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        System.out.println("Built UserDetails with authorities: " + userDetails.getAuthorities());
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

    @Override
    public ApiResponse<?> gettingStudentProfile(String token) {
       try  {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token 
        if (!jwtUtils.validateJwtToken(token)) {
            return ApiResponse.error("Invalid or expired token");
        }

        // Extract email from token  
        String email = jwtUtils.getEmailFromJwtToken(token);

        // Get User from  database 
        Optional<UserSchema> userOpt = UserRepo.findByEmail(email);

        if (!userOpt.isPresent()){
            return ApiResponse.error("User not found");
        }

        UserSchema user = userOpt.get();

        // Check if user is a student
        if (user.getRole() != Roles.STUDENT){
            return ApiResponse.error("Access denied. Only students can access this resource.");
        }

        return ApiResponse.success(
            "Profile fetched successfully",
            user, 
            null
        );

       }
       catch(Exception e) {
           return ApiResponse.error("Server error:" + e.getMessage());
       }
    }

    //  Method to update student profile
    @Override
    public ApiResponse<?> updateStudentProfile(BaseSignUpDto baseSignUpDto, String token) {
        try  {
            // Remove "Bearer" prefix if present
            if (token != null && token.startsWith("Bearer")) {
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)) {
                return ApiResponse.error("Invalid or expired token");
            }

            // Extract email from token
            String email = jwtUtils.getEmailFromJwtToken(token);

            // Get User from database 
            Optional<UserSchema> userOpt = UserRepo.findByEmail(email);

            if (!userOpt.isPresent()){
                return ApiResponse.error("User not found");
            }

            UserSchema user = userOpt.get();

            // Check if user is a student
            if (user.getRole() != Roles.STUDENT){
                return ApiResponse.error("Access denied. Only students can update their profile.");
            }

            // Check if  new email already exists (only if email is being changed )
            if (!user.getEmail().equals(baseSignUpDto.getEmail())){
                if (UserRepo.findByEmail(baseSignUpDto.getEmail()).isPresent()) {
                    return ApiResponse.error("Email already exist , please use another email");
                }
            }

            // Update user profile 
            user.setFirstName(baseSignUpDto.getFirstName());
            user.setLastName(baseSignUpDto.getLastName());
            user.setEmail(baseSignUpDto.getEmail());

            // Only update password if a new one is provided 
            if (baseSignUpDto.getPassword() != null && !baseSignUpDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(baseSignUpDto.getPassword()));
            }

            // Save updated user 
            UserSchema updatedUser = UserRepo.save(user);

            //Generate new Jwt token since email might have changed 
            UserDetailsImpl userDetails = UserDetailsImpl.build(updatedUser);
            String newJwt = jwtUtils.generateJwToken(userDetails);

            return ApiResponse.success(
                "Profile updated successfully",
                updatedUser,
                newJwt
            );
        }

        
        catch(Exception e) {
            return ApiResponse.error("Server error:" + e.getMessage());
        }
    
        
    }


    // Method to delete student profile
    @Override
    public ApiResponse<?> deleteStudentProfile(String token) {
       try {
   
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7);
        }

        // Validate token 
        if (!jwtUtils.validateJwtToken(token)) {
            return ApiResponse.error("Invalid or expired token ");
        }

        // Extract email from token 
        String email = jwtUtils.getEmailFromJwtToken(token);

        // Get User from database 
        Optional<UserSchema> userOpt = UserRepo.findByEmail(email);

        if (!userOpt.isPresent()) {
            return ApiResponse.error("User not found");
        }

        UserSchema user = userOpt.get();

        // Check if user is a student
        if (user.getRole() != Roles.STUDENT){
            return ApiResponse.error("Access denied. Only students can delete their profile.");
        }

        // Delete user 
        UserRepo.delete(user);

        return ApiResponse.success(
            "Profile deleted successfully",
            null,
            null
        );
       }
       catch(Exception e) {
         return ApiResponse.error("Server error:" + e.getMessage());
       }
    }

    // Method to get admin profile
    @Override
    public ApiResponse<?> gettingAdminProfile(String token) {
       try {
        // Remove "Bearreer" prefix if present 
        if (token !=null && token.startsWith("Bearer")){
            token = token.substring(7);
        }


        // Validate token 
        if (!jwtUtils.validateJwtToken(token)) {
            return ApiResponse.error("Invalid or expired token");

        }

        // Extract email from token 
        String email = jwtUtils.getEmailFromJwtToken(token);

        // Get User from database 
        Optional<UserSchema> userOpt = UserRepo.findByEmail(email);
        if (!userOpt.isPresent()){
            return ApiResponse.error("User not found");
        }

        UserSchema user = userOpt.get();

        // Check if user is an admin 
        if (user.getRole() != Roles.ADMIN){
            return ApiResponse.error("Access denied. Only admin can access this resource.");
        }

        return ApiResponse.success(
            "Admin profile fetched successfully",
            user,
            null
        );
       }
       catch(Exception e) {
        return ApiResponse.error("Error to getting Admin Profile:'" + e.getMessage());
       }
    }

    // Method to change student role to admin
    @Override
    public ApiResponse<?> ChangingStudentRoletoAdmin(UUID studentId, String token) {
         try {
            // Remove "Bearer" prefix if present 
            if (token != null && token.startsWith("Bearer")) {
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)) {
                return ApiResponse.error("Invalid or expired token");
            }

            // Get Admin User from token 
            String adminEmail = jwtUtils.getEmailFromJwtToken(token);
            Optional<UserSchema> adminOpt = UserRepo.findByEmail(adminEmail);

            if (!adminOpt.isPresent()) {
                return ApiResponse.error("Admin user not found");
            }

            UserSchema admin = adminOpt.get();

            // Verify that  the token belongs to an admin
            if (admin.getRole() != Roles.ADMIN) {
                return ApiResponse.error("Access denied. Only admins can change user roles.");
            }

            // Find student by ID
            Optional<UserSchema> studentOpt = UserRepo.findById(studentId);

            if (!studentOpt.isPresent()){
                return ApiResponse.error("Student not found");
            }

            UserSchema student = studentOpt.get();

            // Verify that the user is a student 
            if (student.getRole() != Roles.STUDENT) {
                return ApiResponse.error("User is not a student.");
            }

            // Change role to ADMIN 
            student.setRole(Roles.ADMIN);

            // save the updated user 
            UserSchema updatedUser = UserRepo.save(student);

            return ApiResponse.success(
                "Student role changed to Admin successfully",
                updatedUser,
                null 
            );
         }catch(Exception e) {
            return ApiResponse.error("Server error: " + e.getMessage());
         }
    }

    @Override
    public ApiResponse<?> gettingAllInStructors(String token) {
        try {

            // Remove "Bearer" prefix if present 
            if (token != null && token.startsWith("Bearer")){
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)){
                return ApiResponse.error("Invalid or expired token");
            }

            // Extract email from token 
            String email = jwtUtils.getEmailFromJwtToken(token);
            if (email == null) {
                return ApiResponse.error("Invalid or expired token");
            }

            // Get admin user from token 
            Optional<UserSchema> adminOpt = UserRepo.findByEmail(email);

            if (!adminOpt.isPresent()) {
                return ApiResponse.error("Admin user not found");
            }

            UserSchema admin = adminOpt.get();

            // Verify that the token belongs to an admin 
            if (admin.getRole() != Roles.ADMIN) {
                return ApiResponse.error("Access denied. Only admins can access this resource.");
            }

            // Find all users with INSTRUCTOR role 
            List<UserSchema> intructors = UserRepo.findByRole(Roles.INSTRUCTOR);

            if (intructors.isEmpty()){
                return ApiResponse.success(
                    "No Instructors found",
                    Collections.emptyList(),
                    null);
            }

            return ApiResponse.success(
                "Instructors fetched successfully",
                intructors,
                null
            );
        }
        catch(Exception e) {
            return ApiResponse.error("Server error:" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> gettingInstructorDetails(UUID instructorId, String token) {
        try  {
            // Remove "Bearer" prefix if present 
            if (token != null && token.startsWith("Bearer")){
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)) {
                return ApiResponse.error("Invalid or expired token");
            }

            // Extract email from token 
            String email = jwtUtils.getEmailFromJwtToken(token);

            // Get admin user from token 
            Optional<UserSchema> adminOpt = UserRepo.findByEmail(email);

            if (!adminOpt.isPresent()){
                return ApiResponse.error("Admin user not found");
            }

            UserSchema admin = adminOpt.get();

            // Verify that  the token belongs to an admin 
            if (admin.getRole() != Roles.ADMIN) {
                return ApiResponse.error("Access denied. Only admins can view instructor details.");
            }

            // Find instructor by ID
            Optional<UserSchema> instructorOpt = UserRepo.findById(UUID.fromString(instructorId.toString()));

            if (!instructorOpt.isPresent()){
                return ApiResponse.error("Instructor not found");
            }

            UserSchema instructor = instructorOpt.get();

            // Verify that the  user is an instructor 
            if (instructor.getRole() != Roles.INSTRUCTOR) {
                return ApiResponse.error("User is not an Instructor.");
            }

            return ApiResponse.success(
                "Instructor details fetched successfullty",
                instructor,
                null 
            );
        }
        catch(Exception e) {
            return ApiResponse.error("Server error:" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> gettingAllStudents(String token) {
        try  {
            // Remove "Bearer" prefix if  present 
            if (token != null && token.startsWith("Bearer")) {
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)) {
                return ApiResponse.error("Invalid or expired token");
            }

            // Extract email from token 
            String email = jwtUtils.getEmailFromJwtToken(token);

            if (email == null) {
                return ApiResponse.error("Invalid or expired token");
            }

            // Get admin user from token
            Optional<UserSchema> adminOpt = UserRepo.findByEmail(email);

            if (!adminOpt.isPresent()) {
                return ApiResponse.error("Admin user not found");
            }

            UserSchema admin = adminOpt.get();

            // Verify that the token belongs to an admin 
            if (admin.getRole() != Roles.ADMIN) {
                return ApiResponse.error("Access denied. Only admins can view all students.");
            }

            // Find all user with Student role 
            List<UserSchema> students = UserRepo.findByRole(Roles.STUDENT);

            if (students.isEmpty()) {
                return ApiResponse.success(
                    "No students found",
                    Collections.emptyList(),
                    null
                );
            }

            return ApiResponse.success(
                "Student fetched successfully",
                students,
                null
            );
        }
        catch(Exception e ) {
            return ApiResponse.error("Server error:" + e.getMessage());
        }
    }

  @Override
public ApiResponse<?> gettingStudentDetails(UUID studentId, String token) {
    try {
        // Remove "Bearer" prefix if present
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7);
        }

        // Validate token 
        if (!jwtUtils.validateJwtToken(token)) {
            return ApiResponse.error("Invalid or expired token");
        }

        // Extract email from token 
        String email = jwtUtils.getEmailFromJwtToken(token);
        if (email == null) {
            return ApiResponse.error("Invalid or expired token");
        }

        // Get admin user from token 
        Optional<UserSchema> adminOpt = UserRepo.findByEmail(email);
        if (!adminOpt.isPresent()) {
            return ApiResponse.error("Admin user not found");
        }

        UserSchema admin = adminOpt.get();

        // Verify that the token belongs to an admin
        if (admin.getRole() != Roles.ADMIN) {
            return ApiResponse.error("Access denied. Only admins can view student details.");
        }

        // Find student by ID - THIS IS THE FIX
        Optional<UserSchema> studentOpt = UserRepo.findById(studentId);
        if (!studentOpt.isPresent()) {
            return ApiResponse.error("Student not found");
        }

        UserSchema student = studentOpt.get();

        // Verify that the user is actually a student
        if (student.getRole() != Roles.STUDENT) {
            return ApiResponse.error("User is not a student.");
        }

        return ApiResponse.success(
            "Student details fetched successfully",
            student,
            null
        );
    }
    catch(Exception e) {
        return ApiResponse.error("Server error:" + e.getMessage());
    }
}

    @Override
    public ApiResponse<?> gettingAllAdmins(String token) {
        try  {
           // Remove "Bearer" prefix if present
           if (token != null && token.startsWith("Bearer")) {
                token = token.substring(7);
            
           }

           // Validate token 
           if (!jwtUtils.validateJwtToken(token)) {
               return ApiResponse.error("Invalid or expired token");
           }

           // Extract email from token 
           String email = jwtUtils.getEmailFromJwtToken(token);
           if (email == null) {
               return ApiResponse.error("Invalid or expired token");
           }

           // Get admin user from token
           Optional<UserSchema> adminOpt = UserRepo.findByEmail(email);
           if (!adminOpt.isPresent()){
                return ApiResponse.error("admin user not found");
           }

           UserSchema admin = adminOpt.get();

           // Verify that the token belongs to an admin 
           if (admin.getRole() != Roles.ADMIN) {
                return ApiResponse.error("Access denied. Only admins can view all admins.");
           }

           // Find all users with ADMIN role 
           List<UserSchema> admins = UserRepo.findByRole(Roles.ADMIN);

           if (admins.isEmpty()) {
                return ApiResponse.success(
                    "no admins found",
                    Collections.emptyList(),
                    null
                );
           }

           return ApiResponse.success(
                "Admins fetched successfully",
                admins,
                null
           );


        }
        catch(Exception e) {
            return ApiResponse.error("Server error: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> ApproveInstructor(UUID instructorId, String token) {
         try {
                  
            // Remove "Bearer" prefix if present
            if (token != null && token.startsWith("Bearer")) {
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)){
                 return ApiResponse.error("Invalid or token");

            }

            // Extract email from token 
            String email = jwtUtils.getEmailFromJwtToken(token);
            if (email == null) {
                return ApiResponse.error("Invalid or expired token");
            }

            // Get admin user from token 
            Optional<UserSchema> adminOpt = UserRepo.findByEmail(email);
            if (!adminOpt.isPresent()) {
                return ApiResponse.error("Admin user not found");
            }

            UserSchema admin = adminOpt.get();

            // Verify that the  token belongs to an admin 
            if (admin.getRole() != Roles.ADMIN) {
                return ApiResponse.error("Access denied. Can approve instructors");
            }

            // Find instructor by ID
            Optional<UserSchema> instructor = UserRepo.findById(instructorId);
            if (!instructor.isPresent()) {
                return ApiResponse.error("Instructor not found");
            }

            UserSchema instructorOpt = instructor.get();


            // Verify that  the user is an instructor 
            if (instructorOpt.getRole() != Roles.INSTRUCTOR) {
                return ApiResponse.error("User is  not an Instrictor");
            }

            // Verify that the intructor has instructor data
            if (instructorOpt.getInstructorData() == null) {
                return ApiResponse.error("Instructor data not found");
            }

            // Update approval status to APPROVED
            instructorOpt.getInstructorData().setApprovalStatus(Approval.APPROVED);

            // Save the updated instructor
            UserSchema updateInstructor = UserRepo.save(instructorOpt);

            return ApiResponse.success(
                "Instructor approved successfully",
                updateInstructor,
                null
            );
         }
         catch(Exception e) {
            return ApiResponse.error("error" + e.getMessage());
         }
    }

   @Override
public ApiResponse<?> RejectInstructor(UUID instructorId, String token) {
    try {
        // Remove "Bearer" prefix if present
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7);
        }

        // Validate token 
        if (!jwtUtils.validateJwtToken(token)) {
            return ApiResponse.error("Invalid or expired token");
        }

        // Extract email from token 
        String email = jwtUtils.getEmailFromJwtToken(token);
        if (email == null) {
            return ApiResponse.error("Invalid token: email not found");
        }

        // Get admin user from token
        Optional<UserSchema> adminOpt = UserRepo.findByEmail(email);
        if (!adminOpt.isPresent()) {
            return ApiResponse.error("Admin user not found");
        }

        UserSchema admin = adminOpt.get();

        // Verify that the token belongs to an admin
        if (admin.getRole() != Roles.ADMIN) {
            return ApiResponse.error("Access denied. Only admins can reject instructors.");
        }

        // Find instructor by ID
        Optional<UserSchema> instructorOpt = UserRepo.findById(instructorId);
        if (!instructorOpt.isPresent()) {
            return ApiResponse.error("Instructor not found");
        }

        UserSchema instructor = instructorOpt.get();

        // Verify that the user is an instructor
        if (instructor.getRole() != Roles.INSTRUCTOR) {
            return ApiResponse.error("User is not an instructor");
        }

        // Delete the instructor and their associated data
        UserRepo.delete(instructor);

        return ApiResponse.success(
            "Instructor rejected and deleted successfully",
            null,
            null
        );
    }
    catch(Exception e) {
        return ApiResponse.error("Server error: " + e.getMessage());
    }
}



   //Methods for instructor profile management


   // Method  for  getting instructor profile 
   @Override
   public ApiResponse<?> gettingInstructorProfile(String token) {
     try {
        // Remove "Bearer" prefix if present
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7);
        }

        // Validate token 
        if (!jwtUtils.validateJwtToken(token)) {
            return ApiResponse.error("Invalid or expired token");
        }

        // Extract email from token
        String email = jwtUtils.getEmailFromJwtToken(token);
        if (email == null) {
            return ApiResponse.error("Invalid token: email not found");
        }

        // Get User from database 
        Optional<UserSchema> userOpt = UserRepo.findByEmail(email);
        if (!userOpt.isPresent()){
            return ApiResponse.error("User not found");
        }

        UserSchema user = userOpt.get();

        // Check if user is an instructor 
        if (user.getRole() != Roles.INSTRUCTOR) {
             return ApiResponse.error("Access denied. Only instructors can access this resource.");
        }

         // Check if instructor data exists 
         if (user.getInstructorData() == null) {
            return ApiResponse.error("Instructor profile data not found");
         }

         // Check if instructor is approved 
         if (user.getInstructorData().getApprovalStatus() != Approval.APPROVED) {
            return ApiResponse.error("Your Instructor account is pending approval.");
         }

         return ApiResponse.success(
            "Instructor profile fetched successfully",
            user,
            null
         );

     }
     catch (Exception e ) {
         return ApiResponse.error("Server error:" + e.getMessage());
     }
   }


   @Override
   public ApiResponse<?> updateInstructorProfile(InstructorSignUpDto instructorDto, String token) {
    try {

        // Remove "Bearer" prefix if present 
        if (token != null && token.startsWith("Bearer")){
             token = token.substring(7);
        }

        // Validate token 
        if (!jwtUtils.validateJwtToken(token)){
            return ApiResponse.error("Invalid or expired token");
        }

        // Extract email from token 
        String email = jwtUtils.getEmailFromJwtToken(token);
        if (email == null){
            return ApiResponse.error("Invalid token: email not found");
        }

        //Get User from database 
        Optional<UserSchema> userOpt = UserRepo.findByEmail(email);
        if (!userOpt.isPresent()){
            return ApiResponse.error("User not found");
        }

        UserSchema instructor = userOpt.get();

        // Check if user is an instructor 
        if (instructor.getRole() != Roles.INSTRUCTOR){
            return ApiResponse.error("Access denied. Only instructors can update their profile.");
        }
        
        // Check if new email already exists (only if email is being changed)
        if (!instructor.getEmail().equals(instructorDto.getEmail())) {
            if (UserRepo.findByEmail(instructorDto.getEmail()).isPresent()) {
                return ApiResponse.error("Email already exists, please use another email");
            }
        }

        // Update basic user information
        instructor.setFirstName(instructorDto.getFirstName());
        instructor.setLastName(instructorDto.getLastName());
        instructor.setEmail(instructorDto.getEmail());

        // Update password if provided 
        if (instructorDto.getPassword() != null && !instructorDto.getPassword().isEmpty()) {
            instructor.setPassword(passwordEncoder.encode(instructorDto.getPassword()));
        }

        // Update instructor specific data 
        InstructorData instructorData = instructor.getInstructorData();
        instructorData.setPhoneNumber(instructorDto.getPhoneNumber());
        instructorData.setAreaOfExperience(instructorDto.getAreaOfExperience());
        instructorData.setYearOfExperience(instructorDto.getYearsOfExperience());
        instructorData.setProfessionBio(instructorDto.getProfessionalBio());

        // Handle file updates only if new files are provided 
        try {
            if (instructorDto.getResume() != null && !instructorDto.getResume().isEmpty()){
                String resumeUrl = supabaseStorageService.uploadToInstructorFile(
                    instructorDto.getResume(),
                    "resumes"
                );
                instructorData.setResumeUrl(resumeUrl);
            }

            if (instructorDto.getCertificate() != null && !instructorDto.getCertificate().isEmpty()){
                String certificationUrl = supabaseStorageService.uploadToInstructorFile(
                    instructorDto.getCertificate(),
                    "certificates"
                );
                instructorData.setCertificateUrl(certificationUrl);
            }

            if (instructorDto.getImage() != null && !instructorDto.getImage().isEmpty()) {
                String imagerUrl = supabaseStorageService.uploadToInstructorFile(
                    instructorDto.getImage(),
                    "profile-images"
                );
                instructorData.setImageUrl(imagerUrl);
            }

            // Save updated instructor data
            UserSchema updatedInstructor = UserRepo.save(instructor);
            
            // Generate new Jwt token since email might have changed 
            UserDetailsImpl userDetails = UserDetailsImpl.build(updatedInstructor);
            String newJwt = jwtUtils.generateJwToken(userDetails);

            return ApiResponse.success(
                "Instructor profile updated successfully",
                updatedInstructor,
                newJwt
            );
        }catch (Exception e){
            return ApiResponse.error("Error uploading files:" + e.getMessage());
        }

    }
    catch(Exception e) {
        return ApiResponse.error("Server error: " + e.getMessage());
    }
   }


   @Override
   public ApiResponse<?> deleteInstructorProfile(String token) {
      try {
          
           // Remove "Bearer" prefix if present 
           if (token != null && token.startsWith("Bearer")) {
                token = token.substring(7);
           }

           // Validate token 
           if (!jwtUtils.validateJwtToken(token)) {
                return ApiResponse.error("Invalid or expired token");
           }

           // Extact email from token 
           String email = jwtUtils.getEmailFromJwtToken(token);
           if (email == null) {
                return ApiResponse.error("Invalid token: email not found");
           }

           // Get User from database 
           Optional<UserSchema> userOpt = UserRepo.findByEmail(email);
           if (!userOpt.isPresent()){
                return ApiResponse.error("User not found");
           }

           UserSchema instructor = userOpt.get();

           // Check if user is an instructor
           if (instructor.getRole() != Roles.INSTRUCTOR){
                return ApiResponse.error("Access denied. Only instructors can delete their profile.");
           }

           // Check  if instructor data exists 
           if (instructor.getInstructorData() == null) {
                return ApiResponse.error("Instructor profile data not found");
           }

           try {

                // Delete file from storage if they exist 
                if (instructor.getInstructorData().getResumeUrl() != null) {
                     supabaseStorageService.deleteFile(
                        instructor.getInstructorData().getResumeUrl(),
                        "resumes"
                     );
                }

                if (instructor.getInstructorData().getCertificateUrl() != null) {
                        supabaseStorageService.deleteFile(
                            instructor.getInstructorData().getCertificateUrl(),
                            "certificates"
                        );
                }

                if (instructor.getInstructorData().getImageUrl() != null) {
                        supabaseStorageService.deleteFile(
                            instructor.getInstructorData().getImageUrl(),
                            "profile-images"
                        );
                }

           }
           catch (Exception e) {
                return ApiResponse.error("Error deleting instructor files: " + e.getMessage());
                
           }

           // Delete instructor from database
           UserRepo.delete(instructor);

           return ApiResponse.success(
                "Instructor profile deleted successfully",
                null,
                null
           );


      }
      catch(Exception e) {
        return ApiResponse.error("Server error:" + e.getMessage());
      }
   }


    

}

   



    

