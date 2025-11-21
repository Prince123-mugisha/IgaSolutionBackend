package com.work.IGA.Services.CourseServices;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.work.IGA.Models.Courses.AssignmentSubmission;
import com.work.IGA.Models.Courses.AssignmentsSchema;
import com.work.IGA.Models.Courses.Grades;
import com.work.IGA.Utils.AssignmentUtils.SubmissionRequest;
import com.work.IGA.Utils.AssignmentUtils.SubmissionStatisticsDto;
import com.work.IGA.Utils.SubmitionAssignmentUtils.GradeRequest;
import com.work.IGA.Utils.SubmitionAssignmentUtils.SubmissionResponse;
import com.work.IGA.Utils.SubmitionAssignmentUtils.SubmissionDto;

public interface SubmitAssignmentService {


    // Student submission  methods
    SubmissionResponse<SubmissionDto> submitAssignment(SubmissionRequest request, UUID studentId);
    SubmissionResponse<SubmissionDto> updateSubmission(UUID submissionId, MultipartFile newFile, UUID studentId);
    SubmissionResponse<String> deleteSubmission(UUID submissionId, UUID studentId);

    // Student query methods 
    SubmissionResponse<List<SubmissionDto>> getStudentSubmissions(UUID studentId, UUID courseId);
    SubmissionResponse<SubmissionDto> getSubmissionById(UUID submissionId, UUID studentId);
    SubmissionResponse<List<AssignmentsSchema>> getAvailableAssignments(UUID studentId, UUID courseId); 
    
    // Instructor grading  methods 
    SubmissionResponse<Grades> gradeSubmission(GradeRequest request, UUID instructorId);
    SubmissionResponse<Grades> updateGrade(UUID gradeId, GradeRequest request, UUID instructorId);

    // Instructor query methods 
    SubmissionResponse<List<SubmissionDto>> getSubmissionsForAssignment(UUID assignmentId, UUID instructorId);
    SubmissionResponse<List<SubmissionDto>> getSubmissionsForCourse(UUID courseId, UUID instructorId);
    SubmissionResponse<SubmissionStatisticsDto> getSubmissionStatics(UUID assignmentInd, UUID instructorId);
    SubmissionResponse<List<SubmissionDto>> getUngradedSubmission(UUID courseId, UUID instructorId);
}