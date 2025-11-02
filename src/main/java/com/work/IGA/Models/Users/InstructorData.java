package com.work.IGA.Models.Users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "instructor_data")
@AllArgsConstructor
@NoArgsConstructor
public class InstructorData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 15, nullable = false)
    private String phoneNumber;

    @Column(length = 100, nullable = false)
    private String areaOfExperience;

    @Column(length = 10, nullable = false)
    private String yearOfExperience;

    @Column(name = "profession_bio", length = 500, nullable = false)
    private String professionBio;

    @Column(name = "resume_url", length = 255, nullable = false)
    private String resumeUrl;

    @Column(name = "certificate_url", length = 255, nullable = false)
    private String certificateUrl;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_instructor_user"))
    @JsonBackReference
    private UserSchema user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Approval approvalStatus = Approval.PENDING;
}
