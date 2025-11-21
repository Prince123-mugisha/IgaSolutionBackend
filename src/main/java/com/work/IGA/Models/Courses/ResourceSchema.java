package com.work.IGA.Models.Courses;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;  // ← ADD THIS

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resources")
public class ResourceSchema {

    public enum ResourceType {
        VIDEO,
        SLIDE,
        DOCUMENT,
        LINK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    @JsonBackReference  
    private Modules module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;
    
    @Column(nullable = false)
    private String title;

    private String fileUrl;

    private String link;

    @Column(length = 1000)
    private String description;
}