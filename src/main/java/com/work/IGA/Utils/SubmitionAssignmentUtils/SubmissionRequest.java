package com.work.IGA.Utils.SubmitionAssignmentUtils;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {

    private UUID assignmentId;

    private MultipartFile submissionFile;

    

   
}
