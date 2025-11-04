package com.work.IGA.Services.CourseServices;

import java.util.UUID;

import com.work.IGA.Utils.CoursesException.CourseRequest;
import com.work.IGA.Utils.CoursesException.CoursesResponse;

public interface CoursesService {
        
   public CoursesResponse createCourse(CourseRequest request, String token);
   public CoursesResponse getAllCourses();
   public CoursesResponse geCourseById(UUID courseId);
   public CoursesResponse updateCourse(UUID courseId, CourseRequest request, String token);
   public CoursesResponse deleteCourse(UUID courseId, String token);
   public CoursesResponse ratingCourse (UUID courseId , String token);
   
    
}
