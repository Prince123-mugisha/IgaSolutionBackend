package com.work.IGA.Utils.CoursesException;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseRequest {
    @NotBlank(message = "Course name cannot be blank")
    private String courseName;

    @NotBlank(message = "Course description cannot be blank")
    @Size(max = 1000, message = "Course description cannot exceed 1000 characters")
    private String courseDescription;

    @NotNull(message = "Course duration cannot be null")
    @Min(value = 1, message = "Course duration must be at least 1 hour")
    private int durationInHours;

    private MultipartFile image;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private double price;
    
    
}
