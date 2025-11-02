package com.work.IGA.Models.Users;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.UUID;

@Getter
@Setter
@Table(name = "users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserSchema {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private InstructorData instructorData;
}
