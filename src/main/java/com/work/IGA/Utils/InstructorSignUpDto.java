package com.work.IGA.Utils;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class InstructorSignUpDto extends BaseSignUpDto {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Area of experience is required")
    private String areaOfExperience;

    @NotBlank(message = "Years of experience is required")
    @Size(max = 10, message = "Years of experience must not exceed 10 characters")
    private String yearsOfExperience;

    @NotBlank(message = "Professional bio is required")
    @Size(max = 500, message = "Professional bio must not exceed 500 characters")
    private String professionalBio;

    @NotNull(message = "Resume is required")
    private MultipartFile resume;

    @NotNull(message = "Certificate is required")
    private MultipartFile certificate;

    @NotNull(message = "Image is required")
    private MultipartFile image;
}