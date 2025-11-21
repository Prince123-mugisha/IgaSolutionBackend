package com.work.IGA.Utils.ResourseException;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.work.IGA.Models.Courses.ResourceSchema.ResourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceDto {
    private UUID id;
    
    @NotNull(message = "Resource type is required")
    private ResourceType resourceType;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private MultipartFile fileUrl;
    
    private String link;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Module ID is required")
    private UUID moduleId;
}
