package com.work.IGA.Utils.ModuleExceptions;

import java.util.List;
import java.util.UUID;

import com.work.IGA.Utils.AssignmentUtils.AssignmentDto;
import com.work.IGA.Utils.ResourseException.ResourceDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor
@NoArgsConstructor
public class ModuleDto {
    private UUID id;
    private String title;
    private String description;
    private int position;

    // Course reference
    private UUID courseId;
    
    // Optional nested data - USE DTOs, NOT ENTITIES
    private List<ResourceDto> resources;  // ← Changed from ResourceSchema
    private List<AssignmentDto> assignments; // ← Changed from AssignmentSchema
}