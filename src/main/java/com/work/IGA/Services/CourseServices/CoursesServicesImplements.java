package com.work.IGA.Services.CourseServices;

import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.context.support.BeanDefinitionDsl.Role;
import org.springframework.stereotype.Service;

import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Configuration.SupabaseStorageService;
import com.work.IGA.Models.Courses.CoursesSchema;
import com.work.IGA.Models.Users.Roles;
import com.work.IGA.Models.Users.UserSchema;
import com.work.IGA.Repositories.CourseRepo;
import com.work.IGA.Repositories.UserRepository;
import com.work.IGA.Utils.CoursesException.CourseNotFoundException;
import com.work.IGA.Utils.CoursesException.CourseRequest;
import com.work.IGA.Utils.CoursesException.CourseValidationException;
import com.work.IGA.Utils.CoursesException.CoursesResponse;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor
public class CoursesServicesImplements implements CoursesService{

    private final CourseRepo courseRepo;
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;
    private final SupabaseStorageService supabaseStorageService;


    @Override
    public CoursesResponse createCourse(CourseRequest request, String token) {
        try {
            // Remove "Bearer" prefix if present
            if (token != null && token.startsWith("Bearer")) {
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)){
                throw new CourseValidationException("Invalid or expired token");
            }

            // Get  instructor from database 
            String instructorEmail = jwtUtils.getEmailFromJwtToken(token);

            // Get  Instructor from database 
            Optional<UserSchema> instructor = userRepo.findByEmail(instructorEmail);
            if (instructor.isEmpty()){
                throw new CourseValidationException("Instructor not found");
            }

            // Verify that user is an instructor 
            if (instructor.get().getRole() != Roles.INSTRUCTOR){
                 throw new CourseValidationException("Only instructor can create courses");
            }

            // handle image upload to supabase storage
            String imageUrl = null;
            if (request.getImage() != null && !request.getImage().isEmpty()){
                try {
                    // Upload image to Supabase storage 
                    imageUrl = supabaseStorageService.uploadToCourseFile(
                        request.getImage(),
                        "course-images/"+ instructor.get().getId().toString()
                    );

                }
                catch (Exception e) {
                    throw new CourseValidationException("Image upload failed: " + e.getMessage());
                }
            }

            // Create new course 
            CoursesSchema newCourse = new CoursesSchema();
            newCourse.setCourseName(request.getCourseName());
            newCourse.setCourseDescription(request.getCourseDescription());
            newCourse.setDurationInHours(request.getDurationInHours());
            newCourse.setImageUrl(imageUrl);
            newCourse.setPrice(request.getPrice());
            newCourse.setInstructor(instructor.get());
            newCourse.setRating(0.0);

            // save course to datbase 
            CoursesSchema savedCourse = courseRepo.save(newCourse);
               
            // return success response
            return new CoursesResponse(
                    "Course created successfully",
                    true,
                    savedCourse
            );
            
           
        } catch (Exception e) {
            return new  CoursesResponse(
               "Failed to create course: " + e.getMessage(),
                false,
                null
            );
        }
    }

    @Override
    public CoursesResponse getAllCourses() {
         try  {
            // Retrieve all courses from the database
            List<CoursesSchema> courses = courseRepo.findAll();
            
            // Check if courses exist 
            if (courses.isEmpty()) {
                return new CoursesResponse(
                    "No courses found",
                    true ,
                    Collections.emptyList()
                );
            }

            // Return success response with courses 
            return new CoursesResponse(
                "Courses retrieved successfully",
                true
                , courses
            );
         }
         catch (Exception e) {
            return new CoursesResponse(
                "Failed to retrieve courses:" + e.getMessage(),
                false,
                null
            );
         }
    }

    @Override
    public CoursesResponse geCourseById(UUID courseId) {
        try  {
            // Validate course Id 
            if (courseId == null) {
                 return new CoursesResponse(
                    courseId + "is not a valid course ID",
                    false,
                    null
                 );
            }

            // Find course by ID 
            Optional<CoursesSchema> courseOpt = courseRepo.findById(courseId);

            // Check if course exists 
            if (courseOpt.isEmpty()) {
                throw new CourseNotFoundException("Course not found with Id:" + courseId);
            }

            // Return success response with course details 
            return new CoursesResponse(
                "Course retrieved successfully",
                true,
                courseOpt.get()
            );

        }
        catch (Exception e) {
            return new CoursesResponse(
                "Failed to retrieve course:" + e.getMessage(),
                false,
                null
            );
        }
    }

    @Override
    public CoursesResponse updateCourse(UUID courseId, CourseRequest request, String token) {
        try {
            
            // Remove "Bearer" prefix if present 
            if (token != null && token.startsWith("Bearer")) {
                 token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)) {
                throw new CourseValidationException("Invalid or expired token");
            }

            // Get instructor email from token 
            String instructorEmail = jwtUtils.getEmailFromJwtToken(token);

            // Get instructor from database 
            Optional<UserSchema> instructor = userRepo.findByEmail(instructorEmail);
            if (instructor.isEmpty()) {
                throw new CourseValidationException("Instructor not found");
            }

            // Verify that user is an instructor 
            if (instructor.get().getRole() != Roles.INSTRUCTOR){
                    throw new CourseValidationException("Only instructor can  update courses");
            }

            // Find course by Id
            Optional<CoursesSchema> courseOpt = courseRepo.findById(courseId);
            if (courseOpt.isEmpty()) {
                throw new CourseNotFoundException("Course not found with Id:" + courseId);
            }

            CoursesSchema existingCourse = courseOpt.get();

            // Verify that the instructor is the owner of the course
            if (!existingCourse.getInstructor().getId().equals(instructor.get().getId())) {
                throw new CourseValidationException("You can only update your own courses");
            }

            // Handle image upload if new image is provided 
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                try {
                    // If there's an existing image URL, try to delete it
                    if (existingCourse.getImageUrl() != null && !existingCourse.getImageUrl().isEmpty()) {
                        try {
                            supabaseStorageService.deleteFile(
                                existingCourse.getImageUrl(),
                                "course-images/" + instructor.get().getId().toString()
                            );
                        } catch (Exception e) {
                            // Log the error but continue with the update
                            System.err.println("Warning: Failed to delete old image: " + e.getMessage());
                            // Don't throw exception here, continue with upload of new image
                        }
                    }

                    // Upload new image 
                    String newImageUrl = supabaseStorageService.uploadToCourseFile(
                        request.getImage(),
                        "course-images/" + instructor.get().getId().toString()
                    );
                    existingCourse.setImageUrl(newImageUrl);

                }catch (Exception e) {
                     throw new CourseValidationException("Image upload  failed : " + e.getMessage());
                }
            }

            // Update course details 
            existingCourse.setCourseName(request.getCourseName());
            existingCourse.setCourseDescription(request.getCourseDescription());
            existingCourse.setDurationInHours(request.getDurationInHours());
            existingCourse.setPrice(request.getPrice());

            // Save updated course to database 
            CoursesSchema updatedCoursesSchema  = courseRepo.save(existingCourse);

            // Return success response 
            return new CoursesResponse(
                "Course updated successfully",
                true,
                updatedCoursesSchema
            );
        }
        catch (Exception e) {
            return new CoursesResponse(
                "Failed to update course:" + e.getMessage(),
                false,
                null
            );
        }
    }

    @Override
    public CoursesResponse deleteCourse(UUID courseId, String token) {
          try  {
   
              // Remove "Bearer" prefix if present 
              if (token != null && token.startsWith("Bearer")) {
                     token = token.substring(7);
              }

              // Validate token 
              if (!jwtUtils.validateJwtToken(token)){
                  throw new CourseValidationException("Invalid or expired token");
              }
             
              // Get instructor email from token 
              String instructorEmail = jwtUtils.getEmailFromJwtToken(token);

              // Get instructor from database 
              Optional<UserSchema> instructor = userRepo.findByEmail(instructorEmail);
              if (instructor.isEmpty()) {
                throw new CourseValidationException("Instructor not found");
              }
              
              // Veridy  that  user is an instructor 
              if (instructor.get().getRole() != Roles.INSTRUCTOR) {
                    throw new CourseNotFoundException("Only instructor can delete courses");
              }
               // Find course Id 
               Optional<CoursesSchema> courseOpt = courseRepo.findById(courseId);
               if (courseOpt.isEmpty()) {
                    throw new CourseNotFoundException("Course not found with Id" + courseId);
               }

               CoursesSchema course = courseOpt.get();

               // Verify  that  the instructor  owns this  course 
               if (!course.getInstructor().getId().equals(instructor.get().getId())) {
                 throw new CourseValidationException("You can only delete your own courses");
               }

               // Delete course image from storage if it exits 
               if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
                    try  {
                        supabaseStorageService.deleteFile(
                            course.getImageUrl(),
                            "course-image/" + instructor.get().getId().toString()
                        );

                    }
                    catch (Exception e ) {
                        System.err.println("Fail to  delete course image : " + e.getMessage());
                    }
               }

               // Delete the  course from  database 
               courseRepo.delete(course);

               // Return  success response 
               return new CoursesResponse(
                 "Course deleted successfully",
                 true,
                 null
               ); 

            }
          catch (Exception e) {
            return new CoursesResponse(
                "Failed to delete course:" + e.getMessage(),
                false,
                null
            );
          }
    }

    @Override
    public CoursesResponse ratingCourse(UUID courseId, String token) {
         
         try {
             
            if (token != null && token.startsWith("Bearer")) {
                    token = token.substring(7);
            }

            // Validate token 
             if (!jwtUtils.validateJwtToken(token)) {
                   throw new CourseValidationException("Invalid or expired token");
             }

             // Get  student email  from token 
             String studentEmail = jwtUtils.getEmailFromJwtToken(token);

             // Get student from database 
             Optional<UserSchema> student = userRepo.findByEmail(studentEmail);
             if (student.isEmpty()) {
                   throw new CourseValidationException("Student not found");
             }

             // Verify that user is a student 
             if (student.get().getRole() != Roles.STUDENT) {
                     throw new CourseValidationException("Only student can rate courses");
             }

             // Find course by Id 
             Optional<CoursesSchema> courseOpt = courseRepo.findById(courseId);
             if (courseOpt.isEmpty()) {
                     throw new CourseNotFoundException("Course not found with  id : " + courseId);
             }

              CoursesSchema course = courseOpt.get();

              // Calculate rating  (10% = 1 star rating)
              double currentRating = course.getRating();
              double newRating;

              if (currentRating == 0) {
                // First rating 
                newRating = 1.0;

              }
               else {
                newRating = (currentRating + 1.0) / 2.0;
               }

               // Update course rating 
               course.setRating(newRating);

               // Save updated course to database 
               CoursesSchema updatedCourse = courseRepo.save(course);

               // return success response 
               return new CoursesResponse(
                 "Course rated successfully",
                    true,
                    updatedCourse
               );
         }
         catch(Exception e) {
             return new CoursesResponse(
                "Failed to  Rating course: " + e.getMessage(),
                false,
                null
             );
         }
    }
    
}
