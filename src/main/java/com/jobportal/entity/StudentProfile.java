package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    // Basic Info
    private String phoneNumber;
    private String location;
    private String headline;

    // Academic & Skills
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentSkill> skills = new ArrayList<>();

    private String education;
    private String experience;

    // Resume
    private String resumeUrl;
    private String resumeFileName;

    // Projects & Links
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<Project> projects;

    private String githubUrl;
    private String linkedinUrl;
    @Column(columnDefinition = "TEXT")
    private String resumeText;
    // Extras
    private String achievements;
    private String profileImageUrl;
}