package com.work.IGA.Controllers.Courses;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.work.IGA.Services.CourseServices.CoursesService;
import com.work.IGA.Utils.CoursesException.CourseRequest;
import com.work.IGA.Utils.CoursesException.CoursesResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CoursesController {

    private final CoursesService courseService;



    @PostMapping("/create")
    public ResponseEntity<CoursesResponse> createCourse(
          @Valid @ModelAttribute CourseRequest request,
        @RequestHeader("Authorization") String token
    ){
        CoursesResponse response = courseService.createCourse(request, token);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all")
    public ResponseEntity<CoursesResponse> getAllCourse() {
        CoursesResponse response   = courseService.getAllCourses();
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CoursesResponse> getCourseById(
        @PathVariable UUID courseId
    ) {
        CoursesResponse response = courseService.geCourseById(courseId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{courseId}")  // Fixed typo here
    public ResponseEntity<CoursesResponse> updateCourse(
          @PathVariable UUID courseId,
            @Valid @ModelAttribute CourseRequest request,
            @RequestHeader("Authorization") String token
    ) {
        CoursesResponse response = courseService.updateCourse(courseId, request, token);
        return ResponseEntity.ok(response);
    }

     @DeleteMapping("/delete/{courseId}")
        public ResponseEntity<CoursesResponse> deleteCourse(
            @PathVariable UUID courseId,
            @RequestHeader("Authorization") String token
        ) {
            CoursesResponse response = courseService.deleteCourse(courseId, token);
            return ResponseEntity.ok(response);
        }

    @PostMapping("/rate/{courseId}")
    public ResponseEntity<CoursesResponse> rateCourse(
        @PathVariable UUID courseId,
        @RequestHeader("Authorization") String token
    ) {
        CoursesResponse response = courseService.ratingCourse(courseId, token);
        return ResponseEntity.ok(response);
    }


}


