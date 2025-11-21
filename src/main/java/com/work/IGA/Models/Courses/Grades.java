package com.work.IGA.Models.Courses;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Grades {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    // Changed: Link to AssignmentSubmission instead of AssignmentsSchema
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_submission_id")
    @JsonBackReference
    private AssignmentSubmission assignmentSubmission;

    @Column
    private int pointAwarded;

    @Column
    private LocalDateTime gradedAt = LocalDateTime.now();
}