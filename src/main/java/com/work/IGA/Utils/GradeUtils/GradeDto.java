package com.work.IGA.Utils.GradeUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeDto {
    private UUID gradeId;
    private UUID assignmentId;
    private UUID studentId;
    private int pointsAwarded;
    private String feedback;
    private LocalDateTime gradedAt;
    
}
