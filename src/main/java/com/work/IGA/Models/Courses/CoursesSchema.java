package com.work.IGA.Models.Courses;

import java.util.List;
import java.util.UUID;

import com.work.IGA.Models.Users.UserSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses_schema")
public class CoursesSchema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "instructor_id")
   @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
   private UserSchema instructor;


   @Column(nullable = false)
   private String courseName;

   @Column(length = 1000, nullable = false)
   private String courseDescription;

   @Column(nullable = false)
   private int durationInHours;

   @Column
   private double rating = 0.0;

   @Column
   private String imageUrl;

   @Column(nullable = false)
   private double price = 0.0;

   @OneToMany(mappedBy = "course", cascade = jakarta.persistence.CascadeType.ALL, fetch = FetchType.LAZY)
   @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "course"})
   private List<Modules> modules;



}
