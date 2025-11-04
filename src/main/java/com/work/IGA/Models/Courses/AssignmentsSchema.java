package com.work.IGA.Models.Courses;

import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assignments_schema")
public class AssignmentsSchema {

    public enum AssignmentType {
        QUIZ,
        MID,
        SUMMATIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Modules module;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentType assignmentType;


   @Column
   private String documentUrl;

   @Column 
   private int maxPoints = 100;

   @Column
   private LocalDate dueDate;
   

}
