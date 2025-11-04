package com.work.IGA.Repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.work.IGA.Models.Courses.CoursesSchema;

public interface CourseRepo extends JpaRepository<CoursesSchema , UUID> {
    
}
