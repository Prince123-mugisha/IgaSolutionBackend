package com.work.IGA.Models.Courses;
import com.work.IGA.Models.Users.UserSchema;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private AssignmentsSchema assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private UserSchema student;

    @Column(nullable = false)
    private String submissionFile;

    @Column(length = 1000)
    private String feedback;

    @Column
    private LocalDateTime submittedAt = LocalDateTime.now();

    // Add bidirectional relationship with JSON management
    @OneToOne(mappedBy = "assignmentSubmission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Grades grades;
}