package com.work.IGA.Models.Courses;

import java.util.UUID;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Modules module;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;
    
    @Column(nullable = false)
    private String title;

    @Column(nullable = false )
    private String url;

    @Column(length = 1000)
    private String description;
    
}
