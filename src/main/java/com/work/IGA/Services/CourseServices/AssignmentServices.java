package com.work.IGA.Services.CourseServices;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.stereotype.Service;

import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Configuration.SupabaseStorageService;
import com.work.IGA.Models.Courses.AssignmentsSchema;
import com.work.IGA.Models.Courses.Modules;
import com.work.IGA.Repositories.AssignmentRepo;
import com.work.IGA.Repositories.ModulesRepository;
import com.work.IGA.Utils.AssignmentUtils.AssignmentDto;
import com.work.IGA.Utils.AssignmentUtils.AssignmentException;
import com.work.IGA.Utils.AssignmentUtils.AssignmentResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentServices  implements AssignmentServicesImpl{
    private final AssignmentRepo assignmentRepo;
    private final ModulesRepository moduleRepo;
    private final JwtUtils jwtUtils;
    private final SupabaseStorageService supabaseStorage;

    @Override
    public AssignmentResponse createAssignment(AssignmentDto assignmentDto, String token) {
         try {
            //Validate  token 
            if (!jwtUtils.validateJwtToken(token)) {
                 throw new AssignmentException("Invalid or expired token");
            }

            // Get the  module 
            Optional<Modules> module = moduleRepo.findById(assignmentDto.getModuleId());
            if (!module.isPresent()) {
                throw new AssignmentException("Module not found with id :" + assignmentDto.getModuleId());
            }

            // Upload document to supabase 
            String documentUrl = null;
            if (assignmentDto.getDocumentUrl() != null &&  !assignmentDto.getDocumentUrl().isEmpty())  {
               String folder = "assignements/" + module.get().getCourse().getId().toString() + "/";
               documentUrl = supabaseStorage.uploadToCourseFile(assignmentDto.getDocumentUrl(), folder);
            }

            // Create the assignment entity
            AssignmentsSchema assignment = new AssignmentsSchema();
            assignment.setModule(module.get());
            assignment.setTitle(assignmentDto.getTitle());
            assignment.setDescription(assignmentDto.getDescription());
            assignment.setAssignmentType(assignmentDto.getAssignmentType());
            assignment.setDocumentUrl(documentUrl);
            assignment.setMaxPoints(assignmentDto.getMaxPoints());
            assignment.setDueDate(LocalDate.parse(assignmentDto.getDueDate()));

            // save assignment 
            AssignmentsSchema savedAssignment = assignmentRepo.save(assignment);

            return new AssignmentResponse(
                true, 
                "Assignment created successfully",
                savedAssignment.getId(),
                201
            );

         }
         catch (Exception e) {
             throw new AssignmentException("Error to  create  the  Assignment");
         }
    }

    @Override
    public AssignmentResponse getAssignmentById(UUID assignmentId) {
       try   {
             Optional<AssignmentsSchema> assignment = assignmentRepo.findById(assignmentId);
             if (!assignment.isPresent()) {
                   throw new AssignmentException("Assignment not found with id : " + assignmentId);
             }

             AssignmentsSchema foundAssingment = assignment.get();

             return new AssignmentResponse(
                true,
                "Assignment fetched successfully",
                foundAssingment.getId(),
                200
             );


       }
       catch (Exception e) {
            throw new AssignmentException("Error to  getting  Assignment By  id :" + assignmentId);
       }

    }

    @Override
    public AssignmentResponse getAllAssignments() {
        try {

            List<AssignmentsSchema> allAssignments = assignmentRepo.findAll();

            if (allAssignments.isEmpty()) {
                  return new AssignmentResponse(
                    false,
                    "No assignments found",
                    null,
                    200
                  );
            }

            return new AssignmentResponse(
                true,
                "All assignments fetched successfully",
                allAssignments,
                200
            );

        }
        catch (Exception e ) {
              throw new AssignmentException("Error to get  All  Assignments");
        }
    }

    @Override
    public AssignmentResponse getAssignmentsByModuleId(UUID moduleId) {
         try {
            // First  verify  if  module  exists
            Optional<Modules> module = moduleRepo.findById(moduleId);
            if (!module.isPresent()) {
                 throw new AssignmentException("Module not  found with  id :" + moduleId);
            }

          // Get all assignment for  the  module 
          List<AssignmentsSchema> moduleAssignment = assignmentRepo.findByModule(module.get());

          if (moduleAssignment.isEmpty()) {
               return new  AssignmentResponse(
                  false,
                  "No assignments found  for  module with  id :" + moduleId,
                  null,
                  200
               );
          }

           return new  AssignmentResponse(
                true,
                "Assignment fetched  successfully for  module",
                moduleAssignment,
                200
           );


         }catch (Exception e) {
              throw new AssignmentException("Error to  getting Assignment by  module Id :" + e.getMessage());
         }
    }

   @Override
public AssignmentResponse updateAssignment(UUID assignmentId, AssignmentDto assignmentDto, String token) {
    try {
        // Validate the token
        if (!jwtUtils.validateJwtToken(token)) {
            throw new AssignmentException("Invalid or expired token");
        }

        // Check if assignment exists
        Optional<AssignmentsSchema> existingAssignment = assignmentRepo.findById(assignmentId);
        if (!existingAssignment.isPresent()) {
            throw new AssignmentException("Assignment not found with id: " + assignmentId);
        }

        AssignmentsSchema assignment = existingAssignment.get();

        // Check if module changed and exists
        if (assignmentDto.getModuleId() != null && 
            !assignment.getModule().getId().equals(assignmentDto.getModuleId())) {
            Optional<Modules> newModule = moduleRepo.findById(assignmentDto.getModuleId());
            if (!newModule.isPresent()) {
                throw new AssignmentException("Module not found with id: " + assignmentDto.getModuleId());
            }
            assignment.setModule(newModule.get());
        }

        // Handle document update if new file provided
        if (assignmentDto.getDocumentUrl() != null && !assignmentDto.getDocumentUrl().isEmpty()) {
            // Delete old document if exists
            if (assignment.getDocumentUrl() != null) {
                try {
                    supabaseStorage.deleteFile(assignment.getDocumentUrl(), 
                        "assignments/" + assignment.getModule().getCourse().getId().toString());
                } catch (Exception e) {
                    // Log error but continue with update
                    System.err.println("Error deleting old document: " + e.getMessage());
                }
            }

            // Upload new document
            String folder = "assignments/" + assignment.getModule().getCourse().getId().toString() + "/";
            String documentUrl = supabaseStorage.uploadToCourseFile(assignmentDto.getDocumentUrl(), folder);
            assignment.setDocumentUrl(documentUrl);
        }

        // Update other fields if provided
        if (assignmentDto.getTitle() != null) {
            assignment.setTitle(assignmentDto.getTitle());
        }
        if (assignmentDto.getDescription() != null) {
            assignment.setDescription(assignmentDto.getDescription());
        }
        if (assignmentDto.getAssignmentType() != null) {
            assignment.setAssignmentType(assignmentDto.getAssignmentType());
        }
        if (assignmentDto.getMaxPoints() > 0) {
            assignment.setMaxPoints(assignmentDto.getMaxPoints());
        }
        if (assignmentDto.getDueDate() != null) {
            assignment.setDueDate(LocalDate.parse(assignmentDto.getDueDate()));
        }

        // Save updated assignment
        AssignmentsSchema updatedAssignment = assignmentRepo.save(assignment);

        return new AssignmentResponse(
            true,
            "Assignment updated successfully",
            updatedAssignment,
            200
        );

    } catch (AssignmentException e) {
        throw e;
    } catch (Exception e) {
        throw new AssignmentException("Error updating assignment: " + e.getMessage());
    }
}   

    @Override
    public AssignmentResponse deleteAssignment(UUID assignmentId, String token) {
       try  {

        // Validate token
        if (!jwtUtils.validateJwtToken(token)) {
            throw new AssignmentException("Invalid or expired token");
        }

        // find   getAssignmentById
        Optional<AssignmentsSchema> assignment = assignmentRepo.findById(assignmentId);
        if (!assignment.isPresent()) {
             throw new AssignmentException("assignment not  found with  this id :" + assignmentId);
        }

        // delete  the  assignment 
        assignmentRepo.deleteById(assignmentId);

        return new AssignmentResponse(
            true,
            "Assignment deleted successfully",
            null,
            200
        );

       }
       catch (AssignmentException e) {
          throw e;
       }
       catch (Exception e ) {
          throw new  AssignmentException("Error to  delete the  Assignment: " + e.getMessage());
       }
    }
    
}
