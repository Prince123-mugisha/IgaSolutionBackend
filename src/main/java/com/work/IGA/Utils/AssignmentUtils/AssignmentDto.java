package com.work.IGA.Utils.AssignmentUtils;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.work.IGA.Models.Courses.AssignmentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDto {
    private UUID id;
    private UUID moduleId;
    private String title;
    private String description;
    private AssignmentType assignmentType;
    private MultipartFile documentUrl;
    private int maxPoints;
    private String dueDate;
    
}
