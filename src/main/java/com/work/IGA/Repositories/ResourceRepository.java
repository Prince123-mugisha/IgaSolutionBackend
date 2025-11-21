package com.work.IGA.Repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.work.IGA.Models.Courses.ResourceSchema;

public interface ResourceRepository extends JpaRepository<ResourceSchema, UUID> {
  
    List<ResourceSchema> findByModuleId(UUID moduleId); 
} 