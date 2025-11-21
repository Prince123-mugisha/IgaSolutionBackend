package com.work.IGA.Services.CourseServices;

import java.util.UUID;

import com.work.IGA.Utils.AssignmentUtils.AssignmentDto;
import com.work.IGA.Utils.AssignmentUtils.AssignmentResponse;

public interface AssignmentServicesImpl {
    AssignmentResponse createAssignment(AssignmentDto assignmentDto, String token);
    AssignmentResponse getAssignmentById(UUID assignmentId);
    AssignmentResponse getAllAssignments();
    AssignmentResponse getAssignmentsByModuleId(UUID moduleId);
    AssignmentResponse updateAssignment(UUID assignmentId, AssignmentDto assignmentDto, String token);
    AssignmentResponse deleteAssignment(UUID assignmentId, String token);

}