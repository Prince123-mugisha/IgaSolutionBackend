package com.work.IGA.Repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.work.IGA.Models.Courses.Modules;

@Repository
public interface ModulesRepository  extends JpaRepository<Modules, UUID>{
     // Find all modules by course ID
    List<Modules> findByCourseId(UUID courseId);
    
    // Find modules by course ID and order by position
    List<Modules> findByCourseIdOrderByPosition(UUID courseId);
    
    // Check if module exists by title and course ID
    boolean existsByTitleAndCourseId(String title, UUID courseId);
    
    // Count modules in a course
    long countByCourseId(UUID courseId);
    
} 
