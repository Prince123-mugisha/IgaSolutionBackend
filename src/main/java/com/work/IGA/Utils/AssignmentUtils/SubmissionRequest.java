package com.work.IGA.Utils.AssignmentUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {
    private UUID assignmentId;
    private MultipartFile submissionFile; // URL to submitted file/document
    private String notes; // Optional notes from student
}