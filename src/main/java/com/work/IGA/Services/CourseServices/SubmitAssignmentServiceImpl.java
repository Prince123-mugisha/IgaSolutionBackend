package com.work.IGA.Services.CourseServices;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.work.IGA.Configuration.SupabaseStorageService;
import com.work.IGA.Models.Courses.AssignmentSubmission;
import com.work.IGA.Models.Courses.AssignmentsSchema;
import com.work.IGA.Models.Courses.CoursesSchema;
import com.work.IGA.Models.Courses.Enrollment;
import com.work.IGA.Models.Courses.Grades;
import com.work.IGA.Models.Users.UserSchema;
import com.work.IGA.Repositories.AssignmentRepo;
import com.work.IGA.Repositories.AssignmentSubmissionRepository;
import com.work.IGA.Repositories.CourseRepo;
import com.work.IGA.Repositories.EnrollmentRepository;
import com.work.IGA.Repositories.GradesRepository;
import com.work.IGA.Repositories.UserRepository;
import com.work.IGA.Utils.AssignmentUtils.SubmissionRequest;
import com.work.IGA.Utils.AssignmentUtils.SubmissionStatisticsDto;
import com.work.IGA.Utils.SubmitionAssignmentUtils.GradeRequest;
import com.work.IGA.Utils.SubmitionAssignmentUtils.SubmissionException;
import com.work.IGA.Utils.SubmitionAssignmentUtils.SubmissionResponse;
import com.work.IGA.Utils.SubmitionAssignmentUtils.SubmissionDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SubmitAssignmentServiceImpl implements SubmitAssignmentService {
    
    

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepo assignmentRepo;
    private final EnrollmentRepository enrollmentRepository;
    private final GradesRepository gradesRepository;
    private final UserRepository userRepository;
    private final CourseRepo courseRepository;
    private final SupabaseStorageService storageService;

    // Helper method to convert AssignmentSubmission to SubmissionDto
    private SubmissionDto convertToDto(AssignmentSubmission submission) {
        SubmissionDto dto = new SubmissionDto();
        dto.setId(submission.getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setAssignmentTitle(submission.getAssignment().getTitle());
        dto.setStudentId(submission.getStudent().getId());
        dto.setStudentName(submission.getStudent().getFirstName() + " " + submission.getStudent().getLastName());
        dto.setSubmissionFile(submission.getSubmissionFile());
        dto.setFeedback(submission.getFeedback());
        dto.setSubmittedAt(submission.getSubmittedAt());
        
        // Handle grade information
        if (submission.getGrades() != null) {
            dto.setGrade(submission.getGrades().getPointAwarded());
            dto.setGradedAt(submission.getGrades().getGradedAt());
            dto.setGraded(true);
        } else {
            dto.setGraded(false);
        }
        
        return dto;
    }

    // =================STUDENT SUBMISSION Methods=====================

    @Override
    public SubmissionResponse<SubmissionDto> submitAssignment(SubmissionRequest request, UUID studentId) {
       try {
          // Debug logging for request
          if (request == null) {
              throw new SubmissionException("REQUEST_NULL", "SubmissionRequest object is null");
          }
          if (request.getAssignmentId() == null) {
              throw new SubmissionException("ASSIGNMENT_ID_NULL", "Assignment ID is null in request");
          }
          if (request.getSubmissionFile() == null) {
              throw new SubmissionException("SUBMISSION_FILE_NULL", "Submission file is null in request");
          }

          // Get assignment
          AssignmentsSchema assignment = assignmentRepo.findById(request.getAssignmentId())
                .orElseThrow(() -> new SubmissionException("ASSIGNMENT_NOT_FOUND", "Assignment not found for ID: " + request.getAssignmentId()));

          // Get student from token
          UserSchema student = userRepository.findById(studentId)
                .orElseThrow(() -> new SubmissionException("STUDENT_NOT_FOUND", "Student not found for ID: " + studentId));

          // Get course from assignment
          if (assignment.getModule() == null || assignment.getModule().getCourse() == null) {
              throw new SubmissionException("COURSE_NOT_FOUND", "Course/module not found for assignment: " + assignment.getId());
          }
          UUID courseId = assignment.getModule().getCourse().getId();

          // Check if student is enrolled in the course
          boolean isEnrolled = enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
          if (!isEnrolled) {
                return SubmissionResponse.error("Student is not enrolled in the course");
          }

          // Check if student has already submitted
          Optional<AssignmentSubmission> existingSubmission = submissionRepository
                .findByAssignmentIdAndStudentId(request.getAssignmentId(), studentId);
          if (existingSubmission.isPresent()) {
                return SubmissionResponse.error("Assignment has already been submitted. Use update instead.");
          }

          // Upload file to storage
          String fileUrl = null;
          try {
              fileUrl = storageService.uploadToInstructorFile(
                  request.getSubmissionFile(),
                  "submissions/" + courseId + "/" + request.getAssignmentId()
              );
          } catch (Exception ex) {
              throw new SubmissionException("FILE_UPLOAD_ERROR", "Error uploading file: " + ex.getMessage());
          }
          if (fileUrl == null || fileUrl.isEmpty()) {
              throw new SubmissionException("FILE_URL_NULL", "File URL is null or empty after upload");
          }

          // Create submission entity
          AssignmentSubmission submission = new AssignmentSubmission();
          submission.setAssignment(assignment);
          submission.setStudent(student);
          submission.setSubmissionFile(fileUrl);
          submission.setSubmittedAt(LocalDateTime.now());

          AssignmentSubmission savedSubmission = null;
          try {
              savedSubmission = submissionRepository.save(submission);
          } catch (Exception ex) {
              throw new SubmissionException("DB_SAVE_ERROR", "Error saving submission to database: " + ex.getMessage());
          }
          if (savedSubmission == null) {
              throw new SubmissionException("SUBMISSION_SAVE_NULL", "Saved submission is null after database save");
          }

          SubmissionDto responseDto = convertToDto(savedSubmission);
          return SubmissionResponse.success("Assignment submitted successfully", responseDto);
       } catch (SubmissionException se) {
           // Rethrow custom exceptions with details
           throw se;
       } catch (Exception e) {
           throw new SubmissionException("UNKNOWN_ERROR", "Error submitting assignment: " + e.getMessage());
       }
    }

    @Override
    public SubmissionResponse<SubmissionDto> updateSubmission(UUID submissionId, MultipartFile newFile,
            UUID studentId) {
          try {

            // Get existing submission 
            AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found", "SUBMISSION_NOT_FOUND"));


          // Verify  ownership 
          if (!submission.getStudent().getId().equals(studentId)) {
               return SubmissionResponse.error("Unauthorized : you can only  update your own submissions");
          }
          // check if asssignment is  past due date 
          if (submission.getAssignment().getDueDate() != null && 
              submission.getAssignment().getDueDate().isBefore(LocalDateTime.now().toLocalDate())) {
                return SubmissionResponse.error("Cannot update the assignment after due date");
              }

         // Check if submission is alresdy graded 
         if (submission.getGrades() != null) {
                return SubmissionResponse.error("Cannot update a submission that has already been graded");
         }

            // Upload new file to storage
            UUID courseId = submission.getAssignment().getModule().getCourse().getId();
            String newFileUrl = storageService.uploadToInstructorFile(
                newFile,
                "submissions/" + courseId + "/" + submission.getAssignment().getId()
            );

            // Update submission 
            submission.setSubmissionFile(newFileUrl);
            submission.setSubmittedAt(LocalDateTime.now());

            AssignmentSubmission updatedSubmission = submissionRepository.save(submission);
            SubmissionDto responseDto = convertToDto(updatedSubmission);

            return SubmissionResponse.success("Submission updated successfully", responseDto);
          }
          catch (Exception e) {
              throw new SubmissionException(
                "Can not  update submission after due date"
              );
          }
    }

    @Override
    public SubmissionResponse<String> deleteSubmission(UUID submissionId, UUID studentId) {
        try {
            // Get submission 
            AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found ", "SUBMISSION_NOT_FOUND"));

            // Verify ownerShip 
            if (!submission.getStudent().getId().equals(studentId)) {
                throw new SubmissionException("Unauthorized: You can only delete your own submissions", "UNAUTHORIZED");
            }

            // check if submission is already graded
            if (submission.getGrades() != null) {
                throw new SubmissionException("Cannot delete a submission that has already been graded", "GRADED_SUBMISSION");
            }

            // Delete submission 
            submissionRepository.delete(submission);

            return SubmissionResponse.success("Submission deleted successfully", "SUBMISSION_DELETED");
        } catch (Exception e) {
            throw new SubmissionException(
                "Error deleting submission: " + e.getMessage()
            );
        }
    }

    @Override
    public SubmissionResponse<List<SubmissionDto>> getStudentSubmissions(UUID studentId, UUID courseId) {
      try {
        // Verify enrollment 
        boolean isEnrolled = enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
        if (!isEnrolled) {
            return SubmissionResponse.error("Student is not enrolled in the course");
        }

        List<AssignmentSubmission> submissions = submissionRepository.findByStudentIdAndCourseId(studentId, courseId);
        List<SubmissionDto> submissionDtos = submissions.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
            
        return SubmissionResponse.success("Submissions retrieved successfully", submissionDtos);
      } catch (Exception e) {
        throw new SubmissionException(
            "Error retrieving submissions: " + e.getMessage()
        );
      }
    }

    @Override
    public SubmissionResponse<List<AssignmentsSchema>> getAvailableAssignments(UUID studentId, UUID courseId) {
        try  {

            // Verify enrollment 
            boolean isEnrolled = enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
            if (!isEnrolled) {
                return SubmissionResponse.error("Student is not enrolled in the course");
            }

            List<AssignmentsSchema> assignments = assignmentRepo.findByCourseId(courseId);
            return SubmissionResponse.success("Assignment retrieved successfully ", assignments);

        }
        catch (Exception e) {
            throw new SubmissionException(
                "Error retrieving available assignments: " + e.getMessage()
            );
        }
    }

        // =================INSTRUCTOR GRADING Methods=====================

    @Override
    public SubmissionResponse<Grades> gradeSubmission(GradeRequest request, UUID instructorId) {
        try {
            // Validate request
            if (request.getSubmissionId() == null || request.getPointsAwarded() == null) {
                return SubmissionResponse.error("Submission ID and points awarded are required");
            }

            // Get submission
            AssignmentSubmission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new SubmissionException("Submission not found", "SUBMISSION_NOT_FOUND"));

            // Verify instructor ownership
            UUID courseInstructorId = submission.getAssignment().getModule().getCourse().getInstructor().getId();
            if (!courseInstructorId.equals(instructorId)) {
                return SubmissionResponse.error("Unauthorized: You can only grade submissions for your own courses");
            }

            // Debug logging for enrollment lookup
            UUID courseId = submission.getAssignment().getModule().getCourse().getId();
            UUID studentId = submission.getStudent().getId();
            System.out.println("[DEBUG] Looking up enrollment for studentId: " + studentId + " and courseId: " + courseId);

            // Get enrollment (fix parameter order)
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new SubmissionException("Enrollment not found for studentId: " + studentId + " and courseId: " + courseId, "ENROLLMENT_NOT_FOUND"));

            // Check if already graded
            if (submission.getGrades() != null) {
                return SubmissionResponse.error("Submission is already graded. Use update grade instead.");
            }

            // Validate points
            int maxPoints = submission.getAssignment().getMaxPoints();
            if (request.getPointsAwarded() > maxPoints) {
                return SubmissionResponse.error("Points awarded cannot exceed maximum points of " + maxPoints);
            }

            // Create grade
            Grades grade = new Grades();
            grade.setEnrollment(enrollment);
            grade.setAssignmentSubmission(submission);
            grade.setPointAwarded(request.getPointsAwarded());
            grade.setGradedAt(LocalDateTime.now());

            Grades savedGrade = gradesRepository.save(grade);

            // Update submission feedback
            if (request.getFeedback() != null && !request.getFeedback().trim().isEmpty()) {
                submission.setFeedback(request.getFeedback());
                submissionRepository.save(submission);
            }

            return SubmissionResponse.success("Submission graded successfully", savedGrade);

        } catch (Exception e) {
            throw new SubmissionException(
                "Error grading submission: " + e.getMessage()
            );
        }
    }

    @Override
    public SubmissionResponse<Grades> updateGrade(UUID gradeId, GradeRequest request, UUID instructorId) {
        try {

            // Get grade 
            Grades grade = gradesRepository.findById(gradeId)
                .orElseThrow(() -> new SubmissionException("Grade not found ", "GRADE_NOT_FOUND"));

          
        
   

            // Verify instructor owns the course (instructorId from JWT token)
            UUID courseInstructorId = grade.getAssignmentSubmission().getAssignment().getModule().getCourse().getInstructor().getId();
            if (!courseInstructorId.equals(instructorId)) {
                return SubmissionResponse.error("Unauthorized: You can only update grades from your courses");
            }

            // Validate points
            int maxPoints = grade.getAssignmentSubmission().getAssignment().getMaxPoints();
            if (request.getPointsAwarded() > maxPoints) {
                return SubmissionResponse.error("Points awarded cannot exceed maximum points: " + maxPoints);
            }

            // Update grade
            grade.setPointAwarded(request.getPointsAwarded());
            grade.setGradedAt(LocalDateTime.now());

            Grades updatedGrade = gradesRepository.save(grade);

            // Update feedback
            if (request.getFeedback() != null) {
                AssignmentSubmission submission = grade.getAssignmentSubmission();
                submission.setFeedback(request.getFeedback());
                submissionRepository.save(submission);
            }

            return SubmissionResponse.success("Grade updated successfully", updatedGrade);

        }
        catch (Exception e) {
            throw new SubmissionException(
                "Error updating grade: " + e.getMessage()
            );
        }
    }

    @Override
    public SubmissionResponse<List<SubmissionDto>> getSubmissionsForAssignment(UUID assignmentId,
            UUID instructorId) {
         try {
            // Get assignment and verify instructor ownership (instructorId from JWT token)
            AssignmentsSchema assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new SubmissionException("Assignment not found", "ASSIGNMENT_NOT_FOUND"));

            UUID courseInstructorId = assignment.getModule().getCourse().getInstructor().getId();
            if (!courseInstructorId.equals(instructorId)) {
                return SubmissionResponse.error("Unauthorized: You can only view submissions from your assignments");
            }

            List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);
            List<SubmissionDto> submissionDtos = submissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

            return SubmissionResponse.success("Submissions retrieved successfully", submissionDtos);

        } catch (SubmissionException e) {
            return SubmissionResponse.error(e.getMessage());
        } catch (Exception e) {
            return SubmissionResponse.error("Failed to retrieve submissions: " + e.getMessage());
        }
    }

    @Override
    public SubmissionResponse<List<SubmissionDto>> getSubmissionsForCourse(UUID courseId, UUID instructorId) {
         try {
            // Verify instructor owns the course (instructorId from JWT token)
            CoursesSchema course = courseRepository.findById(courseId)
                .orElseThrow(() -> new SubmissionException("Course not found", "COURSE_NOT_FOUND"));

            if (!course.getInstructor().getId().equals(instructorId)) {
                return SubmissionResponse.error("Unauthorized: You can only view submissions from your courses");
            }

            List<AssignmentSubmission> submissions = submissionRepository.findByCourseId(courseId);
            List<SubmissionDto> submissionDtos = submissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

            return SubmissionResponse.success("Submissions retrieved successfully", submissionDtos);

        } catch (SubmissionException e) {
            return SubmissionResponse.error(e.getMessage());
        } catch (Exception e) {
            return SubmissionResponse.error("Failed to retrieve submissions: " + e.getMessage());
        }
    }

    @Override
    public SubmissionResponse<SubmissionStatisticsDto> getSubmissionStatics(UUID assignmentInd, UUID instructorId) {
         try {
            // Get assignment and verify instructor ownership (instructorId from JWT token)
            AssignmentsSchema assignment = assignmentRepo.findById(assignmentInd)
                .orElseThrow(() -> new SubmissionException("Assignment not found", "ASSIGNMENT_NOT_FOUND"));

            UUID courseInstructorId = assignment.getModule().getCourse().getInstructor().getId();
            if (!courseInstructorId.equals(instructorId)) {
                return SubmissionResponse.error("Unauthorized: You can only view statistics from your assignments");
            }

            // Calculate statistics
            long totalSubmissions = submissionRepository.countByAssignmentId(assignmentInd);
            long gradedSubmissions = submissionRepository.countGradedByAssignmentId(assignmentInd);
            long ungradedSubmissions = totalSubmissions - gradedSubmissions;
            long lateSubmissions = submissionRepository.countLateSubmissionsByAssignmentId(assignmentInd);
            Double averageGrade = submissionRepository.getAverageGradeByAssignmentId(assignmentInd);

            SubmissionStatisticsDto stats = new SubmissionStatisticsDto();
            stats.setTotalSubmissions(totalSubmissions);
            stats.setGradedSubmissions(gradedSubmissions);
            stats.setUngradedSubmissions(ungradedSubmissions);
            stats.setLateSubmissions(lateSubmissions);
            stats.setAverageGrade(averageGrade);

            return SubmissionResponse.success("Statistics retrieved successfully", stats);

        } catch (SubmissionException e) {
            return SubmissionResponse.error(e.getMessage());
        } catch (Exception e) {
            return SubmissionResponse.error("Failed to retrieve statistics: " + e.getMessage());
        }
    }

   
    @Override
    public SubmissionResponse<List<SubmissionDto>> getUngradedSubmission(UUID courseId, UUID instructorId) {
        try {
            // Verify instructor owns the course (instructorId from JWT token)
            CoursesSchema course = courseRepository.findById(courseId)
                .orElseThrow(() -> new SubmissionException("Course not found", "COURSE_NOT_FOUND"));

            if (!course.getInstructor().getId().equals(instructorId)) {
                return SubmissionResponse.error("Unauthorized: You can only view submissions from your courses");
            }

            List<AssignmentSubmission> submissions = submissionRepository.findUngradedByCourseId(courseId);
            List<SubmissionDto> submissionDtos = submissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

            return SubmissionResponse.success("Ungraded submissions retrieved successfully", submissionDtos);

        } catch (SubmissionException e) {
            return SubmissionResponse.error(e.getMessage());
        } catch (Exception e) {
            return SubmissionResponse.error("Failed to retrieve ungraded submissions: " + e.getMessage());
        }
    }

    @Override
    public SubmissionResponse<SubmissionDto> getSubmissionById(UUID submissionId, UUID studentId) {
          try {

            // check submission exists
        Optional<AssignmentSubmission> submission = submissionRepository.findById(submissionId);
        if (!submission.isPresent()){
            return SubmissionResponse.error("Submission not found");
        }
        
        // verify ownership
        if (!submission.get().getStudent().getId().equals(studentId)) {
            return SubmissionResponse.error("Unauthorized: You can only view your own submissions");
        }

        // return submission
        SubmissionDto responseDto = convertToDto(submission.get());
        return SubmissionResponse.success("Submission retrieved successfully", responseDto);

          }
          catch (Exception e) {
              throw new SubmissionException(
                "Error retrieving submission: " + e.getMessage()
              );
    }

   
}

}