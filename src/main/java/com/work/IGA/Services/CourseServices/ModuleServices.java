package com.work.IGA.Services.CourseServices;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;


import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Models.Courses.Modules;
import com.work.IGA.Repositories.CourseRepo;
import com.work.IGA.Repositories.ModulesRepository;
import com.work.IGA.Utils.ModuleExceptions.ModuleDto;
import com.work.IGA.Utils.ModuleExceptions.ModuleException;
import com.work.IGA.Utils.ModuleExceptions.ModuleResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleServices implements ModuleServiceImpl{

    private final ModulesRepository moduleRepository;
    private final CourseRepo courseRepository;
    private final JwtUtils jwtUtils;
   
    @Override
public ModuleResponse createModule(ModuleDto moduleDto, String token) {
    try {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token
        if (!jwtUtils.validateJwtToken(token)) {
            throw new ModuleException("Invalid or expired token", 401);
        }

        // Get instructor email from token
        String instructorEmail = jwtUtils.getEmailFromJwtToken(token);

        // Find the course
        var course = courseRepository.findById(moduleDto.getCourseId())
            .orElseThrow(() -> new ModuleException("Course not found with ID: " + moduleDto.getCourseId(), 404));

        // Verify that the instructor owns the course
        if (!course.getInstructor().getEmail().equals(instructorEmail)) {
            throw new ModuleException("Unauthorized: You can only create modules for your own courses", 403);
        }

        // Check if module title already exists in the course
        if (moduleRepository.existsByTitleAndCourseId(moduleDto.getTitle(), moduleDto.getCourseId())) {
            throw new ModuleException("Module with this title already exists in the course", 400);
        }

        // Create new module
        Modules newModule = new Modules();
        newModule.setCourse(course);
        newModule.setTitle(moduleDto.getTitle());
        newModule.setDescription(moduleDto.getDescription());
        newModule.setPosition(moduleDto.getPosition());

        // Save module
        Modules savedModule = moduleRepository.save(newModule);

        // Create a simplified response object without circular references
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", savedModule.getId());
        responseData.put("title", savedModule.getTitle());
        responseData.put("description", savedModule.getDescription());
        responseData.put("position", savedModule.getPosition());
        responseData.put("courseId", course.getId());

        // Optional course info
        Map<String, Object> courseInfo = new HashMap<>();
        courseInfo.put("id", course.getId());
        courseInfo.put("courseName", course.getCourseName());
        responseData.put("course", courseInfo);

        return new ModuleResponse(
            "Module created successfully",
            true,
            responseData,
            201
        );

    } catch (ModuleException e) {
        throw e;
    } catch (Exception e) {
        throw new ModuleException("Failed to create module: " + e.getMessage(), 500);
    }
}

    @Override
    public ModuleResponse getModuleById(UUID moduleId) {
         try {
            // Validate module Id 
            if (moduleId == null) {
                 throw new ModuleException("Module Id can  not be null", 400);
            }

            // Find module by Id 
            var module = moduleRepository.findById(moduleId);
            if (module.isEmpty()) {
                    throw new ModuleException("Module not found", 404);
            }

            return new ModuleResponse(
                "Module retrieved successfully", true, module.get(), 200
            );

         }catch (Exception e) {
             throw new ModuleException("Failed to get Module By Id");
         }
    }

    @Override
    public ModuleResponse getAllModulesByCourseId(UUID courseId) {
        try {
            // Validate course Id
            if (courseId == null) {
                   throw new ModuleException("Please course Id  in  required");
            }

            // Check if course exists 
            var course = courseRepository.findById(courseId);
            if (course.isEmpty()) {
                 throw new ModuleException("Module  is  not  found");
            }

            // Get all modules by course Id 
            var modules = moduleRepository.findByCourseIdOrderByPosition(courseId);

            // returning response 
            return new  ModuleResponse(
                "Modules retrieved successfully", true, modules, 200
            );

        }catch (Exception e) {  
             throw new ModuleException("Failed to  get all modules by course Id : " + e.getMessage());
        }
    }

    @Override
    public ModuleResponse updateModule(UUID moduleId, ModuleDto moduleDto) {
         try  {
            // Validate module Id 
            if (moduleId == null) {
                  throw new ModuleException("Module Id can  not be  null");
            }

            // FInd existing module 
            var existingModule = moduleRepository.findById(moduleId);
            if (existingModule.isEmpty()) {
                    throw new ModuleException("Module not found with Id : " +  moduleId, 404);
            }

            // Validate course exists if course Id is updated 
            if (moduleDto.getCourseId() != null &&
                !moduleDto.getCourseId().equals(existingModule.get().getCourse().getId())) {
                    var newCourse = courseRepository.findById(moduleDto.getCourseId());
                    if (newCourse.isEmpty()) {
                         new ModuleException("Course not found with  ID :" + moduleDto.getCourseId(), 404);
                    }

                    existingModule.get().setCourse(newCourse.get());
                }

                // Check for duplicate title in  the  same course 
                if (moduleDto.getTitle() != null &&
                    !moduleDto.getTitle().equals(existingModule.get().getTitle())&&
                    moduleRepository.existsByTitleAndCourseId(moduleDto.getTitle(), existingModule.get().getCourse().getId())) {
                        throw new ModuleException("Module with the same title already exists in this course", 409);
                    }

                // Update module field if provided 
                if (moduleDto.getTitle() != null) {
                    existingModule.get().setTitle(moduleDto.getTitle());
                }
                if (moduleDto.getDescription() != null) {
                     existingModule.get().setDescription(moduleDto.getDescription());
                }
                if (moduleDto.getPosition() > 0) {
                       existingModule.get().setPosition(moduleDto.getPosition());
                }

                // save updated module 
                Modules updatedModule = moduleRepository.save(existingModule.get());

                return new ModuleResponse(
                    "Module updated successfully", true, updatedModule, 200
                );

         }
         catch (Exception e) {
               throw new ModuleException("Error to  updating module:" + e.getMessage());
         }
    }

    @Override
    public ModuleResponse deleteModule(UUID moduleId) {
         try  {
            // Validate module Id
            if (moduleId == null) {
                   throw new ModuleException("Module Id can not be null");
            }

            // Find the module 
            var module = moduleRepository.findById(moduleId);
            if (module.isEmpty()) {
                 throw new ModuleException("Module not found");
            }

            // Delete associated resources and assignments  first (if any) - handled by cascade delete
            if (module.get().getResources() != null && !module.get().getResources().isEmpty()) {
                    module.get().getResources().clear();
            }

            if (module.get().getAssignments() != null && !module.get().getAssignments().isEmpty()) {
                    module.get().getAssignments().clear();
            }

            // Delete the module 
            moduleRepository.delete(module.get());

            return new ModuleResponse(
                "Module deleted successfully", true, null, 200
            );

         }
         catch (Exception e) {
                throw new ModuleException("Failed to delete module :" + e.getMessage(), 500);
         }
    }
    
}
