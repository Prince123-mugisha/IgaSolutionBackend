package com.work.IGA.Models.Courses;

import com.work.IGA.Models.Users.UserSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "modules"})
    private CoursesSchema course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private UserSchema student;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate = LocalDateTime.now();

    @Column
    private String progress = ProgressEnum.NOT_STARTED.name();
    
}

