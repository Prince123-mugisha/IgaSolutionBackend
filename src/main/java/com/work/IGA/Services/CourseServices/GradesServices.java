package com.work.IGA.Services.CourseServices;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.work.IGA.Utils.GradeUtils.GradeDto;
import com.work.IGA.Utils.GradeUtils.GradeResponse;

public interface GradesServices {

    GradeResponse<List<GradeDto>> getGradesForCourse(UUID studentId, UUID courseId);
    GradeResponse<Map<UUID, List<GradeDto>>> getOverallGrades(UUID studentId);
    GradeResponse<List<GradeDto>> getAllGradesForCourse(UUID instructorId, UUID courseId);
    GradeResponse<Map<UUID, Double>> getOverallGradePercentages(UUID studentId);
     
}

    

