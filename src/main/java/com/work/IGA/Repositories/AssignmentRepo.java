package com.work.IGA.Repositories;


import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.work.IGA.Models.Courses.AssignmentsSchema;
import com.work.IGA.Models.Courses.Modules;


public interface  AssignmentRepo extends JpaRepository<AssignmentsSchema, UUID> {
     List<AssignmentsSchema> findByModule(Modules module);

      
    // Find assignments by module
    List<AssignmentsSchema> findByModuleId(UUID moduleId);
    
    // Find assignments by course
    @Query("SELECT a FROM AssignmentsSchema a WHERE a.module.course.id = :courseId")
    List<AssignmentsSchema> findByCourseId(@Param("courseId") UUID courseId);
    
    // Find assignments by instructor
    @Query("SELECT a FROM AssignmentsSchema a WHERE a.module.course.instructor.id = :instructorId")
    List<AssignmentsSchema> findByInstructorId(@Param("instructorId") UUID instructorId);

   
    
}
